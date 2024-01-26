import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PrimeNumberCounter {

    // AtomicLong variables allowing for multi threaded work using them
    private static AtomicLong sumOfPrimes = new AtomicLong(0);
    private static AtomicLong numOfPrimes = new AtomicLong(0);

    // array of AtomicLong numbers that will store the 10 highest primes
    private static AtomicLongArray largestPrimes = new AtomicLongArray(10);

    public static void main(String[] args) {
        int numOfThreads = 8;

        // calculating the range each thread will be working with
        long startRange = 2;
        long endRange = (long) Math.pow(10, 8);
        long rangeSize = (endRange - startRange + 1) / numOfThreads;

        // starting execution time
        double startTime = System.currentTimeMillis();

        // creating a thread pool with a fixed size of 8
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);

        // variables that will help with calculating the range of numbers for each thread
        long currentStart = startRange;
        long currentEnd = currentStart + rangeSize;

        // loop for all 8 threads
        for (int i = 0; i < numOfThreads; i++) {
            // the last thread may have a smaller range, so we account for that
            if (i == numOfThreads - 1) {
                currentEnd = endRange;
            }

            final long taskStart = currentStart;
            final long taskEnd = currentEnd;

            // submiting the task to each individual thread
            executorService.submit(() -> {
                PrimeResult result = countPrimesInRange(taskStart, taskEnd);
                sumOfPrimes.addAndGet(result.sum);
                numOfPrimes.addAndGet(result.count);
                updateLargestPrimes(result.largestPrimes);
            });

            // updating the range of numbers for the next thread
            currentStart = currentEnd + 1;
            currentEnd = currentStart + rangeSize;
        }

        // shutting down multi threaded work
        executorService.shutdown();

        // while loop that waits for all tasks to be complete
        while (!executorService.isTerminated()) {
        }

        // ending executiong time calculation
        long endTime = System.currentTimeMillis();

        // getting total ammount of seconds it took to finish
        double executionTime = (endTime - startTime)/1000;


        // creating the file with results
        String line1 = executionTime + "   " + numOfPrimes.get() + "   " + sumOfPrimes.get();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"))) {
            // Add each word on a new line
          writer.write(line1);
          writer.newLine();
          for (int i = 9; i >= 0; i--) {
            writer.write(largestPrimes.get(i) + " ");
          }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // class for storing all final values, including sum, count and the 10 largest primes
    private static class PrimeResult {
        long sum;
        long count;
        long[] largestPrimes;

        PrimeResult(long sum, long count, long[] largestPrimes) {
            this.sum = sum;
            this.count = count;
            this.largestPrimes = largestPrimes;
        }
    }

    // function that checks for primes in the range of numbers given
    private static PrimeResult countPrimesInRange(long start, long end) {
        long sum = 0;
        long count = 0;
        long[] largestPrimes = new long[10];

        for (int i = 0; i < 10; i++) {
            largestPrimes[i] = 0;
        }

        for (long num = start; num <= end; num++) {
            if (isPrime(num)) {
                sum += num;
                count++;

                // updating the array with 10 highest primes
                updateLargestPrimesArray(largestPrimes, num);
            }
        }

        // returning a class object with the sum, count, and array with primes from that range
        return new PrimeResult(sum, count, largestPrimes);
    }

    // method that replaces smallest number in array with new higher one
    private static void updateLargestPrimesArray(long[] largestPrimes, long num) {
        for (int i = 0; i < 10; i++) {
            if (num > largestPrimes[i]) {
                for (int j = 9; j > i; j--) {
                    largestPrimes[j] = largestPrimes[j - 1];
                }
                largestPrimes[i] = num;
                break;
            }
        }
    }

    // method that checks if a number is prime
    private static boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        }
        for (long i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    // method that updates the AtomicLongArray that stores the 10 highest primes
    private static void updateLargestPrimes(long[] newPrimes) {
        for (int i = 0; i < 10; i++) {
            long current = largestPrimes.get(i);
            if (newPrimes[i] > current) {
                largestPrimes.compareAndSet(i, current, newPrimes[i]);
            }
        }
    }
}
