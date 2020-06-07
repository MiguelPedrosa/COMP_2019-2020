**PROJECT TITLE: Compiler of the Java-- language to Java Bytecodes


**GROUP: 1A

    NAME1: André Filipe de Soveral Torres Lopes dos Santos, NR1: 200505634, GRADE1: 12, CONTRIBUTION1: 15%

    NAME2: Diogo Luís Cerqueira Carneiro da Silva, NR2: 201405742, GRADE2: 9, CONTRIBUTION2: 5%

    NAME3: Luís Miguel Pedrosa de Moura Oliveira Henriques, NR3: 201604343, GRADE3: 20, CONTRIBUTION: 40%

    NAME4: Pedro Hugo Lima Noevo , NR4: 201604725, GRADE4: 20, CONTRIBUTION: 40%

...

GLOBAL Grade of the project: 19

**SUMMARY: 
    This project is a compiler for the Java-- language. 
    The compiler generates valid JVM (Java Virtual Machine) instructions to jasmin, a tool to generate Java bytecodes given assembly programs with JVM instructions.

**EXECUTE: 

    The produced .j file will be stored in the `jFiles`.
    To run our tool, there are 2 alternatives:

        1. Run ./jasmin.sh <name if the j file> (example "./jasmin.sh Life)

        2.
            2.1. Compile with `gradle build`

            2.2.  run `java -jar jasminTest/jasmin.jar jFiles/<name if the j file>.j -d ./jasminTest/libs`

            2.3. run `java -cp jasminTest/libs <name if the j file>`

            2.4. `rm -r ./jasminTest/libs/$@.class`

**DEALING WITH SYNTACTIC ERRORS: (Describe how the syntactic error recovery of your tool does work. Does it exit after the first error?)
    * Our project with exit early if a unmatched token is found.
    * If a while token is meet, then the compiler will do some error recovering if need. This is done by ignoring the first 10 token, until a ')' is found, in which case compilation is resumed. If within those tokens the '{' character isn't found then compilation will terminate with an error.
    * Skiped tokens caused by errors accumulate from one error to another. For example, if there is an error finding a token but it is found during the next 6 tokens, the following errors will only have 4 tokens to find their sought after tokens. This was done because we believe skiping more than 10 tokens is too much information for the compiler to ignore and the programmer should take action to fix the program.

**SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)
    This phase was implemented by analysing the generated AST from the previous stage and using it to firstly generate a Symbol Table.
    * The Symbol Table contains information about each variable declared in the program used as well as it's type and a set of helper variables that were later used for optimazations.
    * An error does not terminate this stage. The compiler will try to gather as many errors as possible before exiting. Each error is then printed with the corresponding line where the error was found. 
    * The compiler can detact if there is a type mismatch between expressions. For exemple, assignments to a variable with a value that has an incorrect type.
    * Uninitialised are detected if they're initialisation isn't condition (using `if` or `while`). In cases where initialisation can't be asserted a warning is given, but compilation doesn't exit.


**INTERMEDIATE REPRESENTATIONS (IRs): (for example, when applicable, briefly describe the HLIR (high-level IR) and the LLIR (low-level IR) used, if your tool includes an LLIR with structure different from the HLIR)
    No Intermediate representation was done for this project.

**CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)
    The project produces a `.j` file which can then be translated using `jasmin.jar` to create a java classfile. This file can be linked and run will other necessary class files.
    All the given goals for code generation were met and the project can generated jasmin compatible code from valid `J--` file.
    The only problem identified so far occurs if using the `-r` flag in an edge case were a given if some function arguments are unused, the algorithm cannpt detect an adjust to this. The following function might not generate valid jasmin code if using this flag:
    ```
        public int foo(int a, int b) {
            return b;
        }
    ```
    This is because the algorithm will discover that both `a`, `b` (and `this`)can reside in the same local and will set a `.limit` of 1, as well as generate instructions like `aload_0` for fetching the `b` argument which is incorrect.
    The code generation has correct stack and locals calculation.
    If the `-o` flag is passed, the following optimizations are enabled:
    * Integer Division: via right shifts if divisor is a power of 2.
    * Integer Multiplication: via left shifts if is a power of 2.
    * Constant Folding: the expression `2 + 4` is compiled to `6`
    * Expression simplification: Several mathmatical properties were applied. Multiplying a number by `0`, return a `0`, etc..
    * Branch elimination: if an `if`'s condition can be determined at compile time, the code from the non-execution branch is removed and the `if` code is completly replaced.


**OVERVIEW: (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)
    Other than `javacc`, there were no external tools or libraries used in this project.
    Each main compiler step (semantic analysis, code generation, ..) has a separete file location and tries to be as isolated as possible, while trying to provide a suitable interface. 
    The main logic is based on recursion where a current does not need to know everything about it's children or parent. It performs the operations it needs to and calls a commum function on it's children where they are decoded into the matching node type and the appropriate function is called. This prevents overcomplicated logic and insures a steadier development and debuging.

**TASK DISTRIBUTION: 

    * André Lopes dos Santos: Contributions to code generation; Compiler complition and testing; Optimizations
    * Diogo Silva: Translation to a JJTree; Contributions to code generation
    * Luís Miguel Henriques: Parser development; Error treatment and recovery mechanisms; Translation to a JJTree; Symbol Table; Semantic Analysis; Code generation for 
    * function invocations, arithmetic expressions, loops and arrays; Compiler complition and testing; Optimizations; Register allocation
    * Pedro Noevo: Parser development; Error treatment and recovery mechanisms; Translation to a JJTree; Symbol Table; Semantic Analysis; Code generation for function invocations, arithmetic expressions, loops and arrays; Compiler complition and testing; Optimizations; Register allocation

**PROS:
    * It generates and runs correctly all the test files provided.
    * The project has support for overloaded function (as long as they have the same return type, but can have different arguments) as well as polymorphism via the use of the special keyword `extends` and by importing the correct functions. This is done by generating a unique signiture for each given method and using it as key to identifie the function scope.
    * Array suport also is present and, although we can only create arrays of type `int`, the project can interact with other types of arrays, for example the argument of the main function. As long as functions are imported that can interact with `Strings`, this argument can be used. This can be tested using the provided test `MainArg.jmm` which simply prints the given command line arguments.

**CONS: (Identify the most negative aspects of your tool)