package com.tobyclemson.lip.refactored.pattern3;

import com.tobyclemson.lip.refactored.common.LookaheadBuffer;
import com.tobyclemson.lip.refactored.common.Token;
import com.tobyclemson.lip.refactored.pattern2.*;
import com.tobyclemson.lip.refactored.pattern2.lexers.FilteringLexer;
import com.tobyclemson.lip.refactored.pattern2.lexers.RuleBasedLexer;
import com.tobyclemson.lip.refactored.pattern2.rules.MultiCharacterRule;
import com.tobyclemson.lip.refactored.pattern2.rules.SingleCharacterRule;
import com.tobyclemson.lip.refactored.pattern3.phrases.*;
import org.junit.Test;

import static org.javafunk.funk.Literals.iterableWith;

public class Pattern3Test {
    /**
     * list     : '[' elements ']' ;       // match bracketed list
     * elements : element (',' element)* ; // match comma-separated list
     * element  : NAME | list ;            // element is name or nested list
     * NAME     : ('a'..'z'|'A'..'Z')+ ;   // NAME is sequence of >=1 letter
     */

    @Test public void parsesListOfDepthOneWithoutError() {
        // Given
        String input = "[a, b ]";
        Parser parser = listParserFor(input);

        // When
        parser.parse();

        // Then no exceptions are thrown
    }

    @Test public void parsesNestedListsWithoutError() {
        // Given
        String input = "[ant, [bear, [cat]], dog]";
        Parser parser = listParserFor(input);

        // When
        parser.parse();

        // Then no exceptions are thrown
    }

    public Lexer listLexerFor(String input) {
        LexerRule eofRule = new SingleCharacterRule(TokenTypes.EOF);
        LexerRule whitespaceRule = new MultiCharacterRule(TokenTypes.WHITESPACE);
        LexerRule commaRule = new SingleCharacterRule(TokenTypes.COMMA);
        LexerRule leftBracketRule = new SingleCharacterRule(TokenTypes.LBRACK);
        LexerRule rightBracketRule = new SingleCharacterRule(TokenTypes.RBRACK);
        LexerRule nameRule = new MultiCharacterRule(TokenTypes.NAME);

        return new FilteringLexer(
                TokenTypes.WHITESPACE,
                new RuleBasedLexer(input,
                        iterableWith(
                                eofRule,
                                whitespaceRule,
                                commaRule,
                                leftBracketRule,
                                rightBracketRule,
                                nameRule)));
    }

    public Parser listParserFor(String input) {
        Lexer lexer = listLexerFor(input);
        TokenReader tokenReader = new TokenReader(lexer);
        LookaheadBuffer<Token> lookaheadBuffer = new LookaheadBuffer<>(tokenReader);

        return new PhraseBasedParser(lookaheadBuffer, listPhrase().get());
    }

    private LazilyConstructedPhrase.Factory listPhrase() {
        return () -> {
            SingleTokenPhrase leftBracketPhrase = new SingleTokenPhrase(TokenTypes.LBRACK);
            SingleTokenPhrase rightBracketPhrase = new SingleTokenPhrase(TokenTypes.RBRACK);
            SingleTokenPhrase namePhrase = new SingleTokenPhrase(TokenTypes.NAME);

            /** list : '[' elements ']' ; // match bracketed list */
            LazilyConstructedPhrase listPhrase = new LazilyConstructedPhrase(listPhrase());

            /** element : NAME | list ; // element is NAME or nested list */
            AlternationPhrase elementPhrase = new AlternationPhrase(iterableWith(namePhrase, listPhrase));

            /** elements : element (',' element)* ; */
            RepetitionPhrase elementsPhrase = new RepetitionPhrase(elementPhrase, TokenTypes.COMMA);

            return new CompositionPhrase(
                    iterableWith(leftBracketPhrase, elementsPhrase, rightBracketPhrase));
        };
    }
}

