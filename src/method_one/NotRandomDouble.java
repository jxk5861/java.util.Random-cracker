package method_one;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotRandomDouble extends NotRandom {
	private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)

	public NotRandomDouble() {

	}

	public NotRandomDouble(long seed) {
		super(seed);
	}

	/**
	 * next(26) makes this more difficult. There are now 4194304 possible seeds.
	 */
	public double nextDouble() {
		int i1 = next(26);
		System.out.println(Long.toBinaryString(seed.get()));
		int i2 = next(27);

		long l = (((long) (i1) << 27) + i2);

		return l * DOUBLE_UNIT;
	}

	/**
	 * Generate a list of possible seeds from the given double. This ends up being
	 * O(4194304). You might expect the list to have many potential seeds since
	 * there are 4 million total guesses for the seed, but this rarely, if ever,
	 * happens.
	 */
	private static List<Long> possibleSeeds(double d) {
		List<Long> seeds = new ArrayList<>();

		// First we need to convert the double to its 26-bit random and 27-bit random
		// parts.

		// This step does not lose any information since it only effects the exponent
		// bits.
		d = d / DOUBLE_UNIT;
		long gen = (long) d;

		int front = (int) (gen >> 27);
		int nextInt = (int) (gen & ((1 << 27) - 1));

		for (int i = 0; i < 1 << (23); i++) {
			long oldseed = (((long) front) << 22) + i;
			long nextseed = (oldseed * multiplier + addend) & mask;
			int nextInt2 = (int) (nextseed >>> (48 - 27));

			if (nextInt == nextInt2) {
				seeds.add(nextseed);
			}
		}

		return seeds;
	}

	/**
	 * Attempt to generate a seed from a single double.
	 */
	private static long getSeed(double gen) {
		// First we need to break the long into its two integer parts.
		List<Long> seeds = possibleSeeds(gen);

		if (seeds.size() == 1) {
			return initialScramble(seeds.get(0));
		}

		return -1;
	}

	public static void main(String[] args) {
		Random random = new Random();
		double gen = random.nextDouble();

		System.out.println("Initializing java.util.Random()");
		System.out.println("Random::nextDouble() returned " + gen);

		long scrambledSeed = getSeed(gen);
		System.out.println("Recovered seed: " + scrambledSeed);
		Random cloned = new Random(scrambledSeed);
		System.out.println("Cloning random...\r\n");

		System.out.printf("%11s %11s\r\n", "Random", "Cloned");
		for (int i = 0; i < 5; i++) {
			System.out.printf("%11f %11f\r\n", random.nextDouble(), cloned.nextDouble());
		}
	}
}
