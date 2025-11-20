#include "FormulaeConfig.hpp"

#include <algorithm>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <random>
#include <string>

// ------------------------------------------------------------------------------------------------
// Function: generateFormula
// ------------------------------------------------------------------------------------------------
// Generates a single propositional logic formula string composed of random literals,
// optionally negated, and connected by random OR ('v') and AND ('^') operators.
//
// Example output: (-a v b) ^ (c)!3
// The trailing "!<number>" indicates the literal count used for this formula.
// ------------------------------------------------------------------------------------------------
std::string generateFormula(
    int numLiterals,
    std::mt19937 &gen,
    std::uniform_int_distribution<int> &probDist,
    std::uniform_int_distribution<int> &literalDist
) {
    std::string formula;
    formula.reserve(numLiterals * 6);  // Preallocate to reduce reallocations
    formula += '(';

    for (int i = 0; i < numLiterals; ++i) {
        // Randomly apply negation with '-'
        if (probDist(gen) <= FormulaeConfig::NOT_PROBABILITY)
            formula += '-';

        // Append a random literal from the allowed character set
        formula += FormulaeConfig::ALLOWED_LITERALS[literalDist(gen)];

        // Append either OR ('v') or the start of a new AND ('^') group
        if (i + 1 < numLiterals) {
            formula += (probDist(gen) <= FormulaeConfig::AND_TO_OR_PROBABILITY)
                           ? " v "
                           : ") ^ (";
        }
    }

    // Close outer grouping and append literal count marker
    formula += ")!" + std::to_string(numLiterals);
    return formula;
}

// ------------------------------------------------------------------------------------------------
// Entry point
// ------------------------------------------------------------------------------------------------
// Generates a set of random propositional logic formulas and writes them to a text file.
// The output file is placed in a "Formulae" directory, and its name encodes the configuration.
// ------------------------------------------------------------------------------------------------
int main() {
    namespace fs = std::filesystem;

    try {
        // -------------------------------------------------------------------------
        // Prepare output directory and file name
        // -------------------------------------------------------------------------
        std::string paramSuffix =
            std::to_string(FormulaeConfig::NUM_UNIQUE_FORMULAS) + "_" +
            std::to_string(FormulaeConfig::MIN_LITERAL_PLACES) + "_" +
            std::to_string(FormulaeConfig::MAX_LITERAL_PLACES) + "_" +
            std::to_string(FormulaeConfig::NOT_PROBABILITY) + "_" +
            std::to_string(FormulaeConfig::AND_TO_OR_PROBABILITY);

        fs::path basePath = fs::path("..") / "Formulae";
        fs::create_directories(basePath);

        fs::path outputFile = basePath / (paramSuffix + ".txt");
        std::ofstream outFile(outputFile, std::ios::trunc);
        if (!outFile) {
            throw std::runtime_error(
                "Error: Could not open output file: " + outputFile.string()
            );
        }

        // -------------------------------------------------------------------------
        // Write header information
        // -------------------------------------------------------------------------
        outFile << "// Generated formulas: " << FormulaeConfig::NUM_UNIQUE_FORMULAS << "\n";
        outFile << "// Literal count range: " << FormulaeConfig::MIN_LITERAL_PLACES << "–"
                << FormulaeConfig::MAX_LITERAL_PLACES << "\n";
        outFile << "// NOT probability: " << FormulaeConfig::NOT_PROBABILITY << "%, "
                << "AND to OR probability: " << FormulaeConfig::AND_TO_OR_PROBABILITY << "%\n\n";

        // -------------------------------------------------------------------------
        // Setup random distributions and formula counts by group
        // -------------------------------------------------------------------------
        int numLiteralLengths =
            FormulaeConfig::MAX_LITERAL_PLACES - FormulaeConfig::MIN_LITERAL_PLACES + 1;
        int formulasPerGroup =
            FormulaeConfig::NUM_UNIQUE_FORMULAS / numLiteralLengths;
        int remainder =
            FormulaeConfig::NUM_UNIQUE_FORMULAS % numLiteralLengths;

        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<int> probDist(1, 100);
        std::uniform_int_distribution<int> literalDist(
            0, static_cast<int>(FormulaeConfig::ALLOWED_LITERALS.size()) - 1
        );

        // -------------------------------------------------------------------------
        // Generate and save formulas
        // -------------------------------------------------------------------------
        int totalGenerated = 0;

        for (int i = 0; i < numLiteralLengths; ++i) {
            int literalCount = FormulaeConfig::MIN_LITERAL_PLACES + i;
            int formulasForThisGroup = formulasPerGroup + (i < remainder ? 1 : 0);

            for (int j = 0; j < formulasForThisGroup; ++j) {
                std::string formula =
                    generateFormula(literalCount, gen, probDist, literalDist);
                outFile << formula << '\n';

                if (++totalGenerated % FormulaeConfig::FLUSH_INTERVAL == 0) {
                    outFile.flush();
                    std::cout << "Flushed after " << totalGenerated << " formulas...\n";
                }
            }
        }

        outFile.close();

        // -------------------------------------------------------------------------
        // Summary output
        // -------------------------------------------------------------------------
        std::cout << "\nGeneration completed.\n";
        std::cout << "Total formulas: " << totalGenerated << "\n";
        std::cout << "Output file: " << outputFile << "\n";

    } catch (const std::exception &ex) {
        std::cerr << "Error: " << ex.what() << "\n";
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}