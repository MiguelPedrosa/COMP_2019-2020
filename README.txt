[2:19 PM] **PROJECT TITLE: Compiler of the Java-- language to Java Bytecodes
**GROUP: 1A

    NAME1: André Filipe de Soveral Torres Lopes dos Santos, NR1: 200505634, GRADE1: 9, CONTRIBUTION1: 10%

    NAME2: Diogo Luís Cerqueira Carneiro da Silva, NR2: 201405742, GRADE2: 10, CONTRIBUTION2: 10%

    NAME3: Luís Miguel Pedrosa de Moura Oliveira Henriques, NR3: 201604343, GRADE3: 20, CONTRIBUTION: 40%

    NAME4: Pedro Hugo Lima Noevo , NR4: 201604725, GRADE4: 20, CONTRIBUTION: 40%

...

GLOBAL Grade of the project: 19

**SUMMARY: (Describe what your tool does and its main features.)

**EXECUTE: 

    To run our tool, there are 2 alternatives:

    1. Run ./jasmin.sh <name if the j file> (example "./jasmin.sh Life)

    2.
    2.1. Compile with "gradle build"

    2.2.  run java -jar jasminTest/jasmin.jar jFiles/<name if the j file>.j -d ./jasminTest/libs

    2.3. run java -cp jasminTest/libs <name if the j file>

    2.4. rm -r ./jasminTest/libs/$@.class

**DEALING WITH SYNTACTIC ERRORS: (Describe how the syntactic error recovery of your tool does work. Does it exit after the first error?)

**SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)

**INTERMEDIATE REPRESENTATIONS (IRs): (for example, when applicable, briefly describe the HLIR (high-level IR) and the LLIR (low-level IR) used, if your tool includes an LLIR with structure different from the HLIR)

**CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

**OVERVIEW: (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)

**TASK DISTRIBUTION: 

    * André Lopes dos Santos: minor contribution to code generation and optimization.

**PROS:
    * It generates and runs correctly all the test files provided.

**CONS: (Identify the most negative aspects of your tool)