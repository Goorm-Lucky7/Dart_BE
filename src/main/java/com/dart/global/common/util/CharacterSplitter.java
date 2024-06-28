package com.dart.global.common.util;

public class CharacterSplitter {

	private static final int HANGUL_SYLLABLES_START = 0xAC00;
	private static final int HANGUL_SYLLABLES_END = 0xD7A3;
	private static final int CHOSEONG_START = 0x1100;
	private static final int CHOSEONG_END = 0x1112;
	private static final int JUNGSEONG_START = 0x1161;
	private static final int JUNGSEONG_END = 0x1175;
	private static final int JONGSEONG_START = 0x11A8;
	private static final int JONGSEONG_END = 0x11C2;
	private static final int COMPATIBILITY_CHO_START = 0x3131;
	private static final int COMPATIBILITY_CHO_END = 0x314E;
	private static final int COMPATIBILITY_JUNG_START = 0x314F;
	private static final int COMPATIBILITY_JUNG_END = 0x3163;

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
			sb.append(splitCharacter(ch));
		}
		return sb.toString();
	}

	private static String splitCharacter(char ch) {
		if (ch >= HANGUL_SYLLABLES_START && ch <= HANGUL_SYLLABLES_END) {
			int index = ch - HANGUL_SYLLABLES_START;
			int choSeongIndex = index / (21 * 28);
			int jungSeongIndex = (index % (21 * 28)) / 28;
			int jongSeongIndex = index % 28;

			char choSeong = CHO_SEONG[choSeongIndex];
			char jungSeong = JUNG_SEONG[jungSeongIndex];
			char jongSeong = JONG_SEONG[jongSeongIndex];

			if (jongSeong == '\0') {
				return "" + choSeong + jungSeong;
			}

			return "" + choSeong + jungSeong + jongSeong;
		}

		return "" + ch;
	}

	public static String splitAndConvertString(String str) {
		StringBuilder sb = new StringBuilder();
		for (char ch : str.toCharArray()) {
			sb.append(splitAndConvertCharacter(ch));
		}
		return sb.toString();
	}

	private static String splitAndConvertCharacter(char ch) {
		if (ch >= CHOSEONG_START && ch <= JONGSEONG_END) {
			int index = ch - HANGUL_SYLLABLES_START;
			int choSeongIndex = index / (21 * 28);
			int jungSeongIndex = (index % (21 * 28)) / 28;
			int jongSeongIndex = index % 28;

			char choSeong = CHO_SEONG[choSeongIndex];
			char jungSeong = JUNG_SEONG[jungSeongIndex];
			char jongSeong = JONG_SEONG[jongSeongIndex];

			if (jongSeong == '\0') { return "" + choSeong + jungSeong; }

			return "" + choSeong + jungSeong + jongSeong;
		} else if (isCompatibilityJaeum(ch)) {
			convertToChoseong(ch);
		}

		return "" + ch;
	}

	private static char convertToChoseong(char ch) {
		if (ch >= COMPATIBILITY_CHO_START && ch <= COMPATIBILITY_CHO_END) {
			return (char) (CHOSEONG_START + (ch - COMPATIBILITY_CHO_START));
		}
		return ch;
	}

	private static boolean isCompatibilityJaeum(char ch) {
		return (ch >= COMPATIBILITY_CHO_START && ch <= COMPATIBILITY_CHO_END);
	}


	public static String mergeKoreanString(String str) {
		StringBuilder sb = new StringBuilder();
		int length = str.length();

		for (int i = 0; i < length;) {
			char ch = str.charAt(i);
			char cho = '\0';
			char jung = '\0';
			char jong = '\0';

			if (isChoSeong(ch)) {
				cho = str.charAt(i);
				i++;
				if (i < length && (isJungSeong(str.charAt(i)))) {
					jung = str.charAt(i);
					i++;
				}
				if (i < length && (isJongSeong(str.charAt(i)))) {
					jong = str.charAt(i);
					i++;
				}
				System.out.printf("cho, jung, jong: U+%04X U+%04X U+%04X (%c%c%c)%n", (int) cho, (int) jung, (int) jong, cho, jung, jong);

				char mergedChar = mergeCharacter(cho, jung, jong);
				sb.append(mergedChar);

				System.out.printf("Append1: U+%04X (%c), Current StringBuilder: %s%n", (int) mergedChar, mergedChar, sb.toString());
			} else {
				sb.append(ch);
				System.out.printf("Append4: U+%04X (%c), Current StringBuilder: %s%n", (int) ch, ch, sb.toString());
				i++;
			}
		}
		return sb.toString();
	}

	private static char mergeCharacter(char cho, char jung, char jong) {
		if (cho != '\0' && jung != '\0' && jong == '\0') {
			return mergeAllCharacterWithoutJongseong(cho, jung);
		} else {
			return mergeAllCharacter(cho, jung, jong);
		}
	}

	private static char mergeAllCharacter(char cho, char jung, char jong) {
		int choSeongIndex = getIndex(CHO_SEONG, cho);
		int jungSeongIndex = getIndex(JUNG_SEONG, jung);
		int jongSeongIndex = getIndex(JONG_SEONG, jong);

		int unicode = HANGUL_SYLLABLES_START + (choSeongIndex * 21 * 28) + (jungSeongIndex * 28) + jongSeongIndex;
		return (char) unicode;
	}

	private static char mergeAllCharacterWithoutJongseong(char cho, char jung) {
		int choSeongIndex = getIndex(CHO_SEONG, cho);
		int jungSeongIndex = getIndex(JUNG_SEONG, jung);

		int unicode = HANGUL_SYLLABLES_START + (choSeongIndex * 21 * 28) + (jungSeongIndex * 28);
		return (char) unicode;
	}

	public static boolean isChoSeong(char ch) {
		return ch >= CHOSEONG_START && ch <= CHOSEONG_END;
	}

	public static boolean isJungSeong(char ch) {
		return ch >= JUNGSEONG_START && ch <= JUNGSEONG_END;
	}

	public static boolean isJongSeong(char ch) {
		return ch >= JONGSEONG_START && ch <= JONGSEONG_END;
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

	private static int getIndex(char[] array, char value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}
}
