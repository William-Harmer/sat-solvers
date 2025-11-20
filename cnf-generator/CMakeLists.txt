# -----------------------------------------------------------------------------
# Minimum CMake version
# -----------------------------------------------------------------------------
cmake_minimum_required(VERSION 3.15)  # Created using CMake 4.2.0

# -----------------------------------------------------------------------------
# Project metadata
# -----------------------------------------------------------------------------
project(
    cnf-generator
    VERSION 1.0.0
    DESCRIPTION "Randomised CNF formulae text file generator"
    LANGUAGES CXX
)

# -----------------------------------------------------------------------------
# Target
# -----------------------------------------------------------------------------
add_executable(FormulaeGenerator
    ${CMAKE_CURRENT_SOURCE_DIR}/src/FormulaeGenerator.cpp
)

# Header include directory
target_include_directories(FormulaeGenerator
    PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Put binaries in a clean bin/ folder
set_target_properties(FormulaeGenerator PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/bin
)