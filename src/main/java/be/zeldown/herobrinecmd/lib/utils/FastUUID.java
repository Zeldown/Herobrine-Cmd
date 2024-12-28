package be.zeldown.herobrinecmd.lib.utils;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.entity.Entity;

import lombok.NonNull;

public class FastUUID {

	private static final boolean USE_JDK_UUID_TO_STRING;

	private static final int     UUID_STRING_LENGTH = 36;
	private static final char[]  HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final long[]  HEX_VALUES = new long[128];

	static {
		int majorVersion = 0;

		try {
			majorVersion = Integer.parseInt(System.getProperty("java.specification.version"));
		} catch (final NumberFormatException silent) {}

		USE_JDK_UUID_TO_STRING = majorVersion >= 9;
	}

	static {
		Arrays.fill(FastUUID.HEX_VALUES, -1);

		FastUUID.HEX_VALUES['0'] = 0x0;
		FastUUID.HEX_VALUES['1'] = 0x1;
		FastUUID.HEX_VALUES['2'] = 0x2;
		FastUUID.HEX_VALUES['3'] = 0x3;
		FastUUID.HEX_VALUES['4'] = 0x4;
		FastUUID.HEX_VALUES['5'] = 0x5;
		FastUUID.HEX_VALUES['6'] = 0x6;
		FastUUID.HEX_VALUES['7'] = 0x7;
		FastUUID.HEX_VALUES['8'] = 0x8;
		FastUUID.HEX_VALUES['9'] = 0x9;

		FastUUID.HEX_VALUES['a'] = 0xa;
		FastUUID.HEX_VALUES['b'] = 0xb;
		FastUUID.HEX_VALUES['c'] = 0xc;
		FastUUID.HEX_VALUES['d'] = 0xd;
		FastUUID.HEX_VALUES['e'] = 0xe;
		FastUUID.HEX_VALUES['f'] = 0xf;

		FastUUID.HEX_VALUES['A'] = 0xa;
		FastUUID.HEX_VALUES['B'] = 0xb;
		FastUUID.HEX_VALUES['C'] = 0xc;
		FastUUID.HEX_VALUES['D'] = 0xd;
		FastUUID.HEX_VALUES['E'] = 0xe;
		FastUUID.HEX_VALUES['F'] = 0xf;
	}

	public static boolean isUUID(final @NonNull String uuid) {
		return uuid.length() == 32 || uuid.length() == 36;
	}

	public static UUID from(final @NonNull String uuid) {
		if (!FastUUID.isUUID(uuid)) {
			throw new IllegalArgumentException("Illegal UUID string: " + uuid);
		}

		if (uuid.length() == 32) {
			return FastUUID.parseWithoutDashes(uuid);
		}

		return FastUUID.parseWithDashes(uuid);
	}

	public static UUID parseWithoutDashes(final @NonNull String uuid) {
		return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32));
	}

	public static UUID parseWithDashes(final @NonNull String uuidSequence) {
		if (uuidSequence.length() != FastUUID.UUID_STRING_LENGTH || uuidSequence.charAt(8) != '-' || uuidSequence.charAt(13) != '-' || uuidSequence.charAt(18) != '-' || uuidSequence.charAt(23) != '-') {
			throw new IllegalArgumentException("Illegal UUID string: " + uuidSequence);
		}

		long mostSignificantBits = FastUUID.getHexValueForChar(uuidSequence.charAt(0)) << 60;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(1)) << 56;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(2)) << 52;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(3)) << 48;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(4)) << 44;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(5)) << 40;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(6)) << 36;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(7)) << 32;

		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(9)) << 28;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(10)) << 24;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(11)) << 20;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(12)) << 16;

		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(14)) << 12;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(15)) << 8;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(16)) << 4;
		mostSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(17));

		long leastSignificantBits = FastUUID.getHexValueForChar(uuidSequence.charAt(19)) << 60;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(20)) << 56;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(21)) << 52;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(22)) << 48;

		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(24)) << 44;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(25)) << 40;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(26)) << 36;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(27)) << 32;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(28)) << 28;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(29)) << 24;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(30)) << 20;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(31)) << 16;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(32)) << 12;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(33)) << 8;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(34)) << 4;
		leastSignificantBits |= FastUUID.getHexValueForChar(uuidSequence.charAt(35));

		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	private static long getHexValueForChar(final char c) {
		try {
			if (FastUUID.HEX_VALUES[c] < 0) {
				throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
			}
		} catch (final ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
		}

		return FastUUID.HEX_VALUES[c];
	}

	public static String toString(final @NonNull Entity entity) {
		return FastUUID.toString(entity.getUniqueId());
	}

	public static String toString(final @NonNull UUID uuid) {
		if (FastUUID.USE_JDK_UUID_TO_STRING) {
			return uuid.toString();
		}

		final long mostSignificantBits = uuid.getMostSignificantBits();
		final long leastSignificantBits = uuid.getLeastSignificantBits();

		final char[] uuidChars = new char[FastUUID.UUID_STRING_LENGTH];

		uuidChars[0]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0xf000000000000000L) >>> 60)];
		uuidChars[1]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x0f00000000000000L) >>> 56)];
		uuidChars[2]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x00f0000000000000L) >>> 52)];
		uuidChars[3]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x000f000000000000L) >>> 48)];
		uuidChars[4]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x0000f00000000000L) >>> 44)];
		uuidChars[5]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x00000f0000000000L) >>> 40)];
		uuidChars[6]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x000000f000000000L) >>> 36)];
		uuidChars[7]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000f00000000L) >>> 32)];
		uuidChars[8]  = '-';
		uuidChars[9]  = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000f0000000L) >>> 28)];
		uuidChars[10] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x000000000f000000L) >>> 24)];
		uuidChars[11] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000000f00000L) >>> 20)];
		uuidChars[12] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000000f0000L) >>> 16)];
		uuidChars[13] = '-';
		uuidChars[14] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x000000000000f000L) >>> 12)];
		uuidChars[15] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x0000000000000f00L) >>> 8)];
		uuidChars[16] = FastUUID.HEX_DIGITS[(int) ((mostSignificantBits & 0x00000000000000f0L) >>> 4)];
		uuidChars[17] = FastUUID.HEX_DIGITS[(int)  (mostSignificantBits & 0x000000000000000fL)];
		uuidChars[18] = '-';
		uuidChars[19] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0xf000000000000000L) >>> 60)];
		uuidChars[20] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x0f00000000000000L) >>> 56)];
		uuidChars[21] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x00f0000000000000L) >>> 52)];
		uuidChars[22] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x000f000000000000L) >>> 48)];
		uuidChars[23] = '-';
		uuidChars[24] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x0000f00000000000L) >>> 44)];
		uuidChars[25] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x00000f0000000000L) >>> 40)];
		uuidChars[26] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x000000f000000000L) >>> 36)];
		uuidChars[27] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000f00000000L) >>> 32)];
		uuidChars[28] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000f0000000L) >>> 28)];
		uuidChars[29] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x000000000f000000L) >>> 24)];
		uuidChars[30] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000000f00000L) >>> 20)];
		uuidChars[31] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000000f0000L) >>> 16)];
		uuidChars[32] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x000000000000f000L) >>> 12)];
		uuidChars[33] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x0000000000000f00L) >>> 8)];
		uuidChars[34] = FastUUID.HEX_DIGITS[(int) ((leastSignificantBits & 0x00000000000000f0L) >>> 4)];
		uuidChars[35] = FastUUID.HEX_DIGITS[(int)  (leastSignificantBits & 0x000000000000000fL)];

		return new String(uuidChars);
	}

}