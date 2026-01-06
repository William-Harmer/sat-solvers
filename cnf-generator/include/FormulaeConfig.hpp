#pragma once
#include <string>

namespace FormulaeConfig {

// ------------------------------------------------------------------------------------------------
// Configuration constants
// ------------------------------------------------------------------------------------------------

/**
 * @brief Number of unique formulas to generate in total.
 */
constexpr int NUM_UNIQUE_FORMULAS = 1000;

/**
 * @brief Minimum number of literals per formula.
 */
constexpr int MIN_LITERAL_PLACES = 1;

/**
 * @brief Maximum number of literals per formula.
 */
constexpr int MAX_LITERAL_PLACES = 500;

/**
 * @brief Probability (in percent) that a literal will be negated with a NOT ('-') operator.
 */
constexpr int NOT_PROBABILITY = 50;

/**
 * @brief Probability (in percent) that an operator between literals will be "v" (OR)
 * instead of generating a new bracketed "AND" segment.
 */
constexpr int AND_TO_OR_PROBABILITY = 50;

/**
 * @brief Number of formulas generated between flushes to disk.
 * Used to prevent excessive memory buffering for very large outputs.
 */
constexpr int FLUSH_INTERVAL = 100000;

/**
 * @brief Allowed literal characters (excludes 'v' since it’s used as an operator).
 */
const std::string ALLOWED_LITERALS = "abcdefghijklmnopqrstuwxyz";

}