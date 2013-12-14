all: compile_parser

compile_parser: parser.cpp tokenPatterns.cpp
	g++ -o parser -Wall parser.cpp tokenPatterns.cpp tests.cpp utils.cpp

run_parser:
	./parser