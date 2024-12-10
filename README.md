# feat_software
Feedback &amp; Evaluation via Automated Tests (FEAT)

This FEAT software takes the configuration of a function, a properly implemented function, and a number of parameters before generating test cases, testing an inputted experimental function against those cases, and giving both summative and formative assessment on the results. It can (and has been) used to grade functionality of student functions in introductory computer science classes.

-- Methodology --

A JSON configuration file is parsed before being used to generate a concise set of test cases that covers all buggy implementations of that function. That is, as few test cases that flag purposefully buggy implementations of the function to create errors, thereby generating a concise set of test cases that is expansive in the number of semantic code issues it can catch. Inputted functons are tested against the test suite and the results are used to both grade the function on accuracy and also provide specific feedback on errors through specific error messages, thrown through type-safe protocols in the Java programming.

This implementation of FEAT is specifically mechanized for Python functions, meaning it interacts with Python files and programs.
