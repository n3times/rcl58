SRCS = $(wildcard *.hlp)
OBJS = $(patsubst %.hlp,%.html,$(SRCS))

all: $(OBJS)

%.html : %.hlp
	../hlp2html/hlp2html $<

clean:
	find . -name "*.html" -exec rm {} \;
