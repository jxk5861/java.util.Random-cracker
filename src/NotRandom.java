import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The NotRandom class is just a rough copy of java.util.Random for explaining
 * how it works.
 */
public class NotRandom {

	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;

	private static final long mask = (1L << 48) - 1;
	
	/**
	 * Return all 65536 possible seeds for a given 32-bit integer.
	 */
	private static List<Long> possibleSeeds(int integer) {
		List<Long> seeds = new ArrayList<>();
		// Since 32 bits of the seed are used to create the integer we can generate a
		// set of potential seeds which will contain the true seed.
		// The integer 0b10000001000110000100010010011111 has the 32 starting bits of
		// the 48 bit seed.
		// The last 16 bits can be guessed since 2^16 is only 65536.
		// seed = 0bAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA???????????????? where the A's are
		// known
		long front = ((long) (integer) << 16) & mask;
		// The last 16 bits of the seed are all 0, so numbers from 0 to
		// 0b1111111111111111 are added to find every possbile seed
		for (int i = 0; i < 1 << 16; i++) {
			seeds.add(front + i);
		}

		return seeds;
	}

	/**
	 * Filter the seeds for all seeds which produce nextInt. (should just be 1
	 * possibility).
	 */
	private static List<Long> nextSeeds(List<Long> seeds, int nextInt) {
		List<Long> possible = new ArrayList<>();

		for (Long oldseed : seeds) {
			// Simply perform Random::nextInt on the potential seed and see if the numbers
			// match.

			// First find the nextseed in the sequence.
			long nextseed = (oldseed * multiplier + addend) & mask;
			// Then generate the next integer from the seed's first 32 bits.
			int nextInt2 = (int) (nextseed >>> (48 - 32));
			// If there is a match we have found a potential seed.
			if (nextInt == nextInt2) {
				possible.add(nextseed);
			}
		}

		return possible;
	}

	/**
	 * Find the current seed of a Random object given its last output (assuming it
	 * is a long). There may or may not be a very rare edge case where there are
	 * multiple potential seeds, in which -1 is returned.
	 */
	private static long getSeed(long gen) {
		int back = (int) (gen);
		int front = (int) ((gen - back) >> 32);

		List<Long> seeds = possibleSeeds(front);
		seeds = nextSeeds(seeds, back);

		if (seeds.size() == 1) {
			return initialScramble(seeds.get(0));
		}

		return -1;
	}

	public static void main(String[] args) {
		Random random = new Random();
		long gen = random.nextLong();

		System.out.println("Initializing java.util.Random()");
		System.out.println("Random::nextLong() returned " + gen);

		long scrambledSeed = getSeed(gen);
		System.out.println("Recovered seed: " + scrambledSeed);
		Random cloned = new Random(scrambledSeed);
		System.out.println("Cloning random...\r\n");

		System.out.printf("%11s %11s\r\n", "Random", "Cloned");
		for (int i = 0; i < 5; i++) {
			System.out.printf("%11d %11d\r\n", random.nextInt(), cloned.nextInt());
		}
	}
	
	private AtomicLong seed;

	public NotRandom(long seed) {
		this.seed = new AtomicLong(initialScramble(seed));
	}

	// Calling java.util.Random(seed) will cause the seed to be scrambled before
	// this.seed is set.
	// This disables that functionality
	// (also, initialScramble(initialScramble(seed)) = seed assuming seed is at most
	// 48 bits long) because of xor
	public NotRandom(long seed, boolean scramble) {
		if (scramble) {
			this.seed = new AtomicLong(initialScramble(seed));
		} else {
			this.seed = new AtomicLong(seed);
		}
	}

	private static long initialScramble(long seed) {
		return (seed ^ multiplier) & mask;
	}

	public AtomicLong getSeed() {
		return seed;
	}

	public int nextInt() {
		return next(32);
	}

	public long nextLong() {
		// Make a long from two integers.

		// Keep in mind if i1 = 0xaaaaaaaa and i2 = 0xbbbbbbbb
		// the result is not necessarily 0xaaaaaaaabbbbbbbb. (important for reversing
		// this step).
		return ((long) (next(32)) << 32) + next(32);
	}

	/**
	 * Return up to 32 bits of pseudo-random information.
	 */
	protected int next(int bits) {
		long oldseed, nextseed;
		AtomicLong seed = this.seed;

		// usually runs once
		do {
			oldseed = seed.get();
			nextseed = (oldseed * multiplier + addend) & mask;
		} while (!seed.compareAndSet(oldseed, nextseed));

		// The most important part of reversing this algorithm.
		// Notice that the random integer is derived from nextseed.
		// Depending on the size of bits, there are 2^(48 - bits) possibilities for the
		// value of the seed.
		// In the case of an integer, this is only 65536!
		return (int) (nextseed >>> (48 - bits));
	}
}
