package com.dart.global.common.util;

public class CharacterProcessor {

	private static final int HANGUL_SYLLABLES_START = 0xAC00;
	private static final int HANGUL_SYLLABLES_END = 0xD7A3;
	private static final char SPACE = ' ';
	private static final String BLANK = "";

	private static final char[] CHO_SEONG = {
		'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ',
		'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
	};

	private static final char[] JUNG_SEONG = {
		'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ',
		'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
	};

	private static final char[] JONG_SEONG = {
		'\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
		'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ',
		'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
	};

	public static String splitString(String str) {
		StringBuilder sb = new StringBuilder();
		for (char ch : str.toCharArray()) {
			sb.append(splitKoreanCharacter(ch));
		}
		return sb.toString();
	}

	public static String mergeString(String str) {
		StringBuilder sb = new StringBuilder();
		int length = str.length();

		for (int i = 0; i < length;) {
			char charAtI = str.charAt(i);
			char cho = SPACE;
			char jung = SPACE;
			char jong = SPACE;

			if(isChoSeong(charAtI)) {
				cho = str.charAt(i++);
				if (i < length && isJungSeong(str.charAt(i))) {
					jung = str.charAt(i++);
				}
				if (i < length && isJongSeong(str.charAt(i))) {
					jong = str.charAt(i++);
				}
				sb.append(mergeKoreanCharacter(cho, jung, jong));
			} else if (isJungSeong(charAtI)) {
				jung = str.charAt(i++);
				sb.append(mergeKoreanCharacter(SPACE, jung, SPACE));
			} else if (isJongSeong(charAtI)) {
				jong = str.charAt(i++);
				sb.append(mergeKoreanCharacter(SPACE, SPACE, jong));
			} else if (charAtI != SPACE || charAtI != '\u0000'){
				sb.append(charAtI);
				i++;
			} else {
				i++;
			}
		}
		return sb.toString();
	}

	public static double getUnicodeScore(String keyword) {
		double score = 0;
		for (int i = 0; keyword != null && i < keyword.length(); i++) {
			char ch = keyword.charAt(i);
			if (ch >= HANGUL_SYLLABLES_START && ch <= HANGUL_SYLLABLES_END) {
				int chCode = ch - HANGUL_SYLLABLES_START;
				int cho = chCode / (21 * 28);
				int jung = (chCode % (21 * 28)) / 28;
				int jong = chCode % 28;
				score = score * 1000000 + (cho * 10000 + jung * 100 + (jong == '\0' ? 0 : jong));
			} else {
				score = score * 1000000 + ch;
			}
		}
		return score;
	}

	private static String splitKoreanCharacter(char ch) {
		if (ch >= HANGUL_SYLLABLES_START && ch <= HANGUL_SYLLABLES_END) {
			int index = ch - HANGUL_SYLLABLES_START;
			int choSeongIndex = index / (21 * 28);
			int jungSeongIndex = (index % (21 * 28)) / 28;
			int jongSeongIndex = index % 28;

			char choSeong = (choSeongIndex < CHO_SEONG.length) ? CHO_SEONG[choSeongIndex] : SPACE;
			char jungSeong = (jungSeongIndex < JUNG_SEONG.length) ? JUNG_SEONG[jungSeongIndex] : SPACE;
			char jongSeong = (jongSeongIndex < JONG_SEONG.length) ? JONG_SEONG[jongSeongIndex] : SPACE;

			return BLANK + choSeong + jungSeong + jongSeong;
		}
		return BLANK + ch;
	}

	private static char mergeKoreanCharacter(char cho, char jung, char jong) {
		int choSeongIndex = getIndex(CHO_SEONG, cho);
		int jungSeongIndex = getIndex(JUNG_SEONG, jung);
		int jongSeongIndex = getIndex(JONG_SEONG, jong);

		if(cho == SPACE && jung == SPACE) { return (char)jong; }
		if(cho == SPACE && jong == SPACE) { return (char)jung; }
		if(jung == SPACE && jong == SPACE) { return (char)cho; }

		int unicode = HANGUL_SYLLABLES_START + (choSeongIndex * 21 * 28) + (jungSeongIndex * 28) + jongSeongIndex;
		return (char)unicode;
	}

	private static int getIndex(char[] array, char value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return 0;
	}

	public static boolean isChoSeong(char ch) {
		for (char c : CHO_SEONG) {
			if (c == ch) {
				return true;
			}
		}
		return false;
	}

	public static boolean isJungSeong(char ch) {
		for (char c : JUNG_SEONG) {
			if (c == ch) {
				return true;
			}
		}
		return false;
	}

	public static boolean isJongSeong(char ch) {
		for (char c : JONG_SEONG) {
			if (c == ch) {
				return true;
			}
		}
		return false;
	}

	public static void printSplitResult(String input) {
		String result = splitString(input);
	}

	public static void printMergeResult(String input) {
		String result = mergeString(input);
	}

	public static void printUnicodeScore(String input) {
		double score = getUnicodeScore(input);
	}
}