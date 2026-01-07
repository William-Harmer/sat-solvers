# A Comparative Analysis of SAT Solvers
This repository contains the code and analysis developed for my undergraduate dissertation, which explores the design, implementation, and evaluation of various SAT solvers. The project begins with basic solver implementations, starting from a brute-force approach and progressing towards more sophisticated and efficient algorithms, with the best-performing model based on the CDCL (Conflict-Driven Clause Learning) technique.

All solvers in this project are designed to handle Boolean formulae expressed in CNF (Conjunctive Normal Form). To support benchmarking, I created a CNF generator that produces a range of test formulae. These are used to compare solver implementations in terms of runtime performance and memory usage, providing insights into how different algorithms scale and optimise under varying problem complexities.

This work was largely inspired by the [SAT Competition](https://satcompetition.github.io/), an annual event that evaluates the performance of state-of-the-art SAT solvers on diverse CNF benchmarks. Its focus on runtime, robustness, and memory efficiency motivated this exploration into the principles and mechanics behind modern solver design and optimisation.

If you’re interested in the full research and analysis, my dissertation is included in the repository and available [here](./dissertation.pdf).
