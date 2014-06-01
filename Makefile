TARGET =	Chespel
CHP_FILE=	example5doc

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
CHP_DIR=	$(ROOT)/examples
TMP_DIR=	$(ROOT)/tmp
FAILE_DIR=	$(ROOT)/src/chessEngine
DEBUG_DIR=	$(ROOT)/debug

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

# NUM is the unique-number generated for the debug file
NUM := $(shell \
    x=""; \
    while [ -z $$x ] || [ -e $(DEBUG_DIR)/debug-$$x.chp ] ; do \
	x=$$(dd count=3 bs=1 if=/dev/urandom 2> /dev/null | xxd -p | tr '[:lower:]' '[:upper:]')  ;\
    done; echo -n $$x | tr -d \ )
				
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

pdf: compile exec
	if [ ! -e $(TMP_DIR) ]; then \
	    mkdir $(TMP_DIR);\
	fi
	$(BIN)/$(TARGET) -nocomp -dot -ast $(TMP_DIR)/ast_generated.dot examples/$(CHP_FILE).chp && dot -Tpdf $(TMP_DIR)/ast_generated.dot -o ast_generated.pdf && rm $(TMP_DIR)/ast_generated.dot 

chp: compile exec
	$(BIN)/$(TARGET) -o generated_eval $(CHP_DIR)/$(CHP_FILE).chp 

faile: compile exec
	$(BIN)/$(TARGET) -o $(FAILE_DIR)/generated_eval $(CHP_DIR)/$(CHP_FILE).chp
	$(MAKE) -C $(FAILE_DIR) all
	$(FAILE_DIR)/faile

xboard: compile exec
	$(BIN)/$(TARGET) -o $(FAILE_DIR)/generated_eval $(CHP_DIR)/$(CHP_FILE).chp
	$(MAKE) -C $(FAILE_DIR) all
	xboard -cp -fd "$(FAILE_DIR)" -fcp "$(FAILE_DIR)/faile"

debug: compile exec
	$(shell \
	    $(BIN)/$(TARGET) $(CHP_DIR)/$(CHP_FILE).chp 2>$(TMP_DIR)/err.txt; \
	    if [ $$? -eq 1 ] ; then \
		cp $(TMP_DIR)/err.txt $(DEBUG_DIR)/debug-$(NUM).txt ; \
		cp $(CHP_DIR)/$(CHP_FILE).chp $(DEBUG_DIR)/debug-$(NUM).chp; \
	    fi)

DEBUG_FILES := $(shell find $(DEBUG_DIR) -iname "*.chp" | rev | cut -c 5- | rev )

check: $(DEBUG_FILES)

$(DEBUG_FILES):
	$(BIN)/$(TARGET) -o $(TMP_DIR)/$(subst $(DEBUG_DIR)/,,$@) $@.chp
