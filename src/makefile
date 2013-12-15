CFLAGS= -Wall

ifeq ($(d), 1)
CFLAGS= -Wall -D_GLIBCXX_DEBUG
endif

all: compile_parser

compile_parser: parser.cpp tokenPatterns.cpp
	g++ $(CFLAGS) -o parser parser.cpp tokenPatterns.cpp tests.cpp utils.cpp

run_parser:
	./parser