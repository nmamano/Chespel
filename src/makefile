CFLAGS= -Wall

ifeq ($(d), 1)
CFLAGS= -Wall -D_GLIBCXX_DEBUG
endif

all: compile_parser

compile_parser: parser.cpp tokenPatterns.cpp
	g++ $(CFLAGS) -o parser parser.cpp tokenPatterns.cpp tests.cpp utils.cpp preprocessing.cpp

run_parser:
	./parser

test:
	./parser < commentsTest.chp > newRes1
	./parser < ../doc/programExample.chp > newRes2
	diff res1 newRes1
	diff res2 newRes2
