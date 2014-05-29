TARGET =	Chespel

# Directories
ROOT =		$(PWD)
SRCDIR = 	$(ROOT)/src
LIBDIR =	$(ROOT)/libs
CLASSDIR = 	$(ROOT)/classes
MAIN =		$(SRCDIR)/$(TARGET)
PARSER =	$(SRCDIR)/parser
COMPILER =	$(SRCDIR)/compiler
JAVADOC =	$(ROOT)/javadoc
BIN =		$(ROOT)/bin

# Executable
EXEC = 		$(BIN)/$(TARGET)
JARFILE =	$(BIN)/$(TARGET).jar
MANIFEST=	$(BIN)/$(TARGET)_Manifest.txt

# Libraries and Classpath
LIB_ANTLR =	$(LIBDIR)/antlr3.jar
LIB_CLI =	$(LIBDIR)/commons-cli.jar
CLASSPATH=	$(LIB_ANTLR):$(LIB_CLI)
JARPATH=	"$(LIB_ANTLR) $(LIB_CLI)"


# Distribution (tar) file
DATE= 		$(shell date +"%d%b%y")
DISTRIB=	$(TARGET)_$(DATE).tgz

# Classpath


# Flags
JFLAGS =	-classpath $(CLASSPATH) -d $(CLASSDIR) 
DOCFLAGS =	-classpath $(CLASSPATH) -d $(JAVADOC) -private

# Source files
GRAMMAR = 		$(PARSER)/$(TARGET).g

MAIN_SRC =		$(MAIN)/$(TARGET).java

PARSER_SRC =	$(PARSER)/$(TARGET)Lexer.java \
				$(PARSER)/$(TARGET)Parser.java
				
COMPILER_SRC =	$(COMPILER)/ChespelCompiler.java \
				$(COMPILER)/TypeInfo.java \
				$(COMPILER)/$(TARGET)Tree.java \
				$(COMPILER)/SymbolTable.java \
				$(COMPILER)/$(TARGET)TreeAdaptor.java \
				$(COMPILER)/CompileException.java \
				$(COMPILER)/ErrorStack.java \
				$(COMPILER)/ConfigOptions.java \
				$(COMPILER)/ChpOption.java

ALL_SRC =		$(MAIN_SRC) $(PARSER_SRC) $(COMPILER_SRC)
				
all: compile exec docs

compile:
	java -jar $(LIB_ANTLR) -o $(PARSER) $(GRAMMAR)
	if [ ! -e $(CLASSDIR) ]; then\
	  mkdir $(CLASSDIR);\
	fi
	javac $(JFLAGS) $(ALL_SRC)

docs:
	javadoc $(DOCFLAGS) $(ALL_SRC)

exec:
	if [ ! -e $(BIN) ]; then\
	  mkdir $(BIN);\
	fi
	echo "Main-Class: Chespel.Chespel" > $(MANIFEST)
	echo "Class-Path: $(JARPATH)" >> $(MANIFEST)
	cd $(CLASSDIR); jar -cmf $(MANIFEST) $(JARFILE) *
	printf "#!/bin/sh\n\n" > $(EXEC)
	printf 'exec java -enableassertions -jar $(JARFILE) "$$@"' >> $(EXEC)
	chmod a+x $(EXEC)
	
clean:
	rm -rf $(PARSER)/*.java $(PARSER)/*.tokens 
	rm -rf $(CLASSDIR)

distrib: clean
	rm -rf $(JAVADOC)
	rm -rf $(BIN)

tar: distrib
	cd ..; tar cvzf $(DISTRIB) $(TARGET); mv $(DISTRIB) $(TARGET); cd $(TARGET)

# Chespel file
CHP_FILE=example_minimalist
TMP_DIR=/tmp
FRAILE_DIR=src/chessEngine/
dot: compile exec
	$(BIN)/$(TARGET) -nocomp -dot -ast $(TMP_DIR)/ast_generated.dot examples/$(CHP_FILE).chp && dot -Tpdf $(TMP_DIR)/ast_generated.dot -o ast_generated.pdf && rm $(TMP_DIR)/ast_generated.dot 
chp: compile exec
	$(BIN)/$(TARGET) examples/$(CHP_FILE).chp 
	if [ -e generated_eval.c ]; then\
	  mv generated_eval.c $(FRAILE_DIR) &&\
	  $(MAKE) -C $(FRAILE_DIR) all;\
	fi
