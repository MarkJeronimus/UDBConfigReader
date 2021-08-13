package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.ASTERISK;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.COMMENT;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.NEWLINE;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.SLASH;

/**
 * @author Zom-B
 */
// Created 2021-08-10
@UtilityClass
public final class CommentsLexer {
	private enum CommentType {
		NONE,
		UNDECIDED, // 1 token in
		LINE,
		BLOCK,
		BLOCK_MAYBE_END
	}

	private CommentsLexer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> process(Collection<ConfigToken> tokens) {
		List<ConfigToken> processedTokens = new ArrayList<>(tokens.size());

		CommentType           commentType       = CommentType.NONE;
		StringBuilder         commentText       = new StringBuilder(1024);
		@Nullable ConfigToken firstCommentToken = null;

		for (ConfigToken token : tokens) {
			ConfigToken.TokenType tokenType = token.getTokenType();

			switch (commentType) {
				case NONE:
					if (tokenType == SLASH) {
						commentType = CommentType.UNDECIDED;
						commentText.append(token.getText());
						firstCommentToken = token;
					} else {
						processedTokens.add(token);
					}

					break;
				case UNDECIDED:
					if (tokenType == SLASH) {
						commentType = CommentType.LINE;
						commentText.append(token.getText());
					} else if (tokenType == ASTERISK) {
						commentType = CommentType.BLOCK;
						commentText.append(token.getText());
					} else {
						processedTokens.add(token);
						commentType = CommentType.NONE;
						commentText.setLength(0);
					}
					break;
				case LINE:
					if (tokenType == NEWLINE) {
						processedTokens.add(firstCommentToken.replace(COMMENT, commentText.toString()));
						processedTokens.add(token);
						commentType = CommentType.NONE;
						commentText.setLength(0);
					} else {
						commentText.append(token.getText());
					}
					break;
				case BLOCK:
					commentText.append(token.getText());
					if (tokenType == ASTERISK)
						commentType = CommentType.BLOCK_MAYBE_END;

					break;
				case BLOCK_MAYBE_END:
					commentText.append(token.getText());
					if (tokenType == SLASH) {
						processedTokens.add(firstCommentToken.replace(COMMENT, commentText.toString()));
						commentType = CommentType.NONE;
						commentText.setLength(0);
					} else if (tokenType != ASTERISK) {
						commentType = CommentType.BLOCK;
					}
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + commentType);
			}
		}

		return processedTokens;
	}
}
