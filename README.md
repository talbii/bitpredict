# bitpredict - Android Project for Israel MOE

This repository holds my Android project for the Israel MOE (ministry of education). This project by itself is valid for 5 pts. (יחידות לימוד).

## Prediction Models

Due to lack of time, and the fact that Java isn't a great language for data processing, there are two prediction methods: polynomial fit, and Markov chain. These two methods are available in `Polynomial.java` and `MarkovChain.java`, respectively. Below is a simple explanation for these two methods.

### Polynomial Fit Prediction

Given $n$ points $(t_1, p_1) \ldots (t_n, p_n) \in \mathbb{R}^2$, where each points represents the price of a coin at timestamp $t_i$ (to be precise, the timestamp is given as an offset from a known timestamp, e.g., the first of January 2022), we fit a polynomial $p$ of degree $n + 1$ (there is only one such polynomial) using an algorithm by Neville.

After doing that, we are left with a polynomial $p \in \mathbb{R}_{n+1}[x]$. We compute the value of this polynomial in the "future" ( $t_n + \text{offset}$ ). The result is our prediction.

Because Neville's algorithm does $O(n^3)$ floating point operations, and the fact that this is runninng locally, on your phone, it is possible to tune the value of $n$ - i.e., how far back to the past should we look.

### Markov Chain Prediction

Instead of mindlessly attempting to fit a polynomial to many past points, we instead consider the following process:

- The price of the coin in the next 5 minutes (i.e., the next "point") should only depend on few past processes.
- Idealy, the next price only depends on the current price.

**Problem:** the price does not depend only on the current price.

*Solution:* we look back far enough, process that data and answer accordingly. Because this algorithm takes little time ( $\Theta(n)$ ), we are actually able to process a large chunk of data (e.g., the last week of prices, with 5 minute intervals) and answer a fairly confident answer.

The calculation itself is done by sampling the history, and checking when we had the following pattern(s):

- The price increased by $a \%$
- The price decreased by $a \%$
- The price did not change ( $\pm \varepsilon$ )

Afterwards, guessing the next price is a matter of probability. 
