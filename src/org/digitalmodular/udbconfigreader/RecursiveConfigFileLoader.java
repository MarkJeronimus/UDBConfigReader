package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.udbconfigreader.lexer.CleaningLexer;
import org.digitalmodular.udbconfigreader.lexer.CommentsLexer;
import org.digitalmodular.udbconfigreader.lexer.ConfigToken;
import org.digitalmodular.udbconfigreader.lexer.ConfigTokenizer;
import org.digitalmodular.udbconfigreader.lexer.KeywordLexer;
import org.digitalmodular.udbconfigreader.lexer.StringsLexer;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STATEMENT_SEPARATOR;

/**
 * Parses the tokens (which should've been preprocessed by a sequence of lexers)
 * into the specified structure, and executes functions it encounters.
 *
 * @author Zom-B
 */
// Created 2021-08-13
public class RecursiveConfigFileLoader {
	private final Pattern backslashPattern = Pattern.compile("\\\\");

	/**
	 * The list of files which are currently being parsed.
	 * Each subsequent element is a file included by the previous element.
	 * The last element is the one currently being parsed.
	 */
	private final Deque<Path>         fileStack         = new ArrayDeque<>(8);
	private final Deque<ConfigStruct> configStructStack = new ArrayDeque<>(8);

	public ConfigStruct loadConfigurationFile(Path file) throws IOException {
		@Nullable ConfigStruct gameConfiguration = ConfigFileCache.INSTANCE.get(file);
		if (gameConfiguration != null)
			return gameConfiguration;

		if (Logger.getGlobal().isLoggable(FINER))
			Logger.getGlobal().log(INFO, "Loading configuration file: " + file.toAbsolutePath());
		else
			Logger.getGlobal().log(INFO, "Loading configuration file: " + file);

		gameConfiguration = new ConfigStruct(file.toString(), 16);

		fileStack.push(file);
		configStructStack.push(gameConfiguration);
		try {
			parseConfigurationFile(file);
		} finally {
			fileStack.pop();
			configStructStack.pop();
		}

		ConfigFileCache.INSTANCE.add(file, gameConfiguration);
		return gameConfiguration;
	}

	private void parseConfigurationFile(Path file) throws IOException {
		try {
			List<String> lines = Files.readAllLines(file);

			CharacterReader reader = new CharacterReader(file.getFileName().toString(), lines);

			List<ConfigToken> tokens = ConfigTokenizer.tokenize(reader);

			tokens = StringsLexer.process(tokens);
			tokens = CommentsLexer.process(tokens);
			tokens = KeywordLexer.process(tokens);
			tokens = CleaningLexer.process(tokens);

			parseTokens(tokens.iterator());
		} catch (IOException ex) {
			throw new IOException("Unable to load the game configuration file: " + file, ex);
		}
	}

	public void parseTokens(Iterator<ConfigToken> tokens) {
		while (tokens.hasNext())
			parseStatement(tokens);
	}

	private void parseStatement(Iterator<ConfigToken> tokens) {
		ConfigToken firstToken = tokens.next();

		switch (firstToken.getTokenType()) {
			case STATEMENT_SEPARATOR:
				return;
			case OTHER:
				parseEntry(firstToken, tokens);
				break;
			default:
				throwSyntaxError(firstToken, "identifier");
		}
	}

	private void parseEntry(ConfigToken firstToken, Iterator<ConfigToken> tokens) {
		ConfigToken token = tokens.next();

		switch (token.getTokenType()) {
			case ASSIGNMENT:
				parseAssignment(firstToken, tokens);
				return;
			case FUNCTION_START:
				parseFunction(firstToken, tokens);
				return;
			case BLOCK_START:
				parseBlock(firstToken, tokens);
				return;
			case STATEMENT_SEPARATOR:
				configStructStack.getFirst().put(firstToken.getText(), null);
				return;
			default:
				throwSyntaxError(token, "\"=\", \"{\", or \"(\" after an identifier");
		}
	}

	private void parseAssignment(ConfigToken firstToken, Iterator<ConfigToken> iter) {
		requireHasNextToken(iter, firstToken, "a variable assignment");
		ConfigToken token = iter.next();

		switch (token.getTokenType()) {
			case STRING:
				configStructStack.getFirst().put(firstToken.getText(), token.getText());
				break;
			case OTHER:
				if (token.getText().equalsIgnoreCase("null")) {
					configStructStack.getFirst().put(firstToken.getText(), null);
					break;
				}

				@Nullable Object value = parseValue(token.getText());
				if (value != null) {
					configStructStack.getFirst().put(firstToken.getText(), value);
					break;
				}

				// fall-through
			default:
				throwSyntaxError(token, "a string, a number, a boolean, or null");
		}

		requireNextTokenOfType(STATEMENT_SEPARATOR, iter, token);
	}

	private static @Nullable Object parseValue(String text) {
		if (text.isEmpty())
			return null;
		else if (text.equalsIgnoreCase("false"))
			return Boolean.FALSE;
		else if (text.equalsIgnoreCase("true"))
			return Boolean.TRUE;

		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException ignored) {
		}

		char lastChar = text.charAt(text.length() - 1);
		if (lastChar == 'f' || lastChar == 'F') {
			try {
				return Float.parseFloat(text);
			} catch (NumberFormatException ignored) {
			}
		} else {
			try {
				return Double.parseDouble(text);
			} catch (NumberFormatException ignored) {
			}
		}

		return null;
	}

	private void parseFunction(ConfigToken firstToken, Iterator<ConfigToken> iter) {
		List<Object> parameters = new ArrayList<>(8);

		boolean     requireValue = true;
		ConfigToken token;
		while (true) {
			requireHasNextToken(iter, firstToken, "a function call");

			token = iter.next();

			switch (token.getTokenType()) {
				case STRING:
					if (!requireValue)
						throwSyntaxError(token, "\",\" or \")\"");

					parameters.add(token.getText());
					break;
				case LIST_SEPARATOR:
					if (requireValue)
						throwSyntaxError(token, "a literal value");

					break;
				case FUNCTION_END:
					callFunction(firstToken, parameters);
					return;
				default:
					throwSyntaxError(token, "nothing");
			}

			requireValue = !requireValue;
		}
	}

	private void callFunction(ConfigToken firstToken, List<Object> parameters) {
		String function = firstToken.getText();
		assert !function.isEmpty();
		if ("include".equals(function.toLowerCase())) {
			callIncludeFunction(firstToken, parameters);
		} else {
			throw new IllegalArgumentException("Unknown function: " + function + ", at " +
			                                   firstToken.getLocationString());
		}
	}

	private void callIncludeFunction(ConfigToken firstToken, List<Object> parameters) {
		if (parameters.isEmpty())
			throw new IllegalArgumentException("include() is missing parameters, at " + firstToken.getLocationString());

		Object filename = parameters.get(0);
		if (!(filename instanceof String))
			throw new IllegalArgumentException("First parameter of include() must be a string, at " +
			                                   firstToken.getLocationString());

		Object section = parameters.size() > 1 ? parameters.get(1) : "";
		if (!(section instanceof String))
			throw new IllegalArgumentException("Second parameter of include() must be a string, at " +
			                                   firstToken.getLocationString());

		String filenameString = backslashPattern.matcher((String)filename).replaceAll("/");

		assert !fileStack.isEmpty();
		Path originalFile = fileStack.getFirst();
		Path includeFile  = originalFile.getParent().resolve(filenameString);

		if (fileStack.contains(includeFile)) {
			fileStack.stream()
			         .map(Path::getFileName)
			         .map(Object::toString)
			         .collect(Collectors.joining("->"));
			throw new IllegalArgumentException("Circular include chain detected: " + fileStack + "->" + includeFile +
			                                   ", at " + firstToken.getLocationString());
		}

		try {
			ConfigStruct block = loadConfigurationFile(includeFile);

			String sectionName = (String)section;
			if (!sectionName.isEmpty()) {
				String[] sectionNameParts = sectionName.split("\\.");

				for (String sectionNamePart : sectionNameParts) {
					@Nullable Object value = block.get(sectionNamePart);

					if (!(value instanceof ConfigStruct))
						throw new IllegalArgumentException("Include is missing requested structure, at " +
						                                   firstToken.getLocationString());

					block = (ConfigStruct)value;
				}
			}

			configStructStack.getFirst().putAll(block);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to read include file: " + filename, ex);
		}
	}

	private void parseBlock(ConfigToken firstToken, Iterator<ConfigToken> iter) {
		requireHasNextToken(iter, firstToken, "a block");

		ConfigStruct block = new ConfigStruct(firstToken.getText(), 16);

		configStructStack.push(block);
		try {
			ConfigToken token;
			while (true) {
				token = iter.next();
				switch (token.getTokenType()) {
					case STATEMENT_SEPARATOR:
						continue;
					case OTHER:
						parseEntry(token, iter);
						break;
					case BLOCK_END:
						return;
					default:
						throwSyntaxError(token, "identifier");
				}
			}
		} finally {
			configStructStack.pop();
			configStructStack.getFirst().put(firstToken.getText(), block);
		}
	}

	private static void requireNextTokenOfType(TokenType tokenType, Iterator<ConfigToken> iter, ConfigToken lastToken) {
		requireHasNextToken(iter, lastToken, "a statement");

		ConfigToken token = iter.next();
		if (token.getTokenType() == tokenType)
			return;

		throwSyntaxError(token, "\";\"");
	}

	private static void requireHasNextToken(Iterator<ConfigToken> iter, ConfigToken lastToken, String parsingThing) {
		if (iter.hasNext())
			return;

		throw new IllegalArgumentException("Unexpected End of file while parsing " + parsingThing +
		                                   " at " + lastToken.getLocationString());
	}

	private static void throwSyntaxError(ConfigToken token, String expectedThing) {
		throw new IllegalArgumentException("Syntax error at " + token.getLocationString() +
		                                   ". Expecting " + expectedThing +
		                                   ", but found: \"" + token.getEscapedText() +
		                                   "\" (" + token.getTokenType() + ')');
	}
}
