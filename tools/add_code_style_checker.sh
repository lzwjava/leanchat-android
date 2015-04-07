#!/bin/sh

#CURRENT_PATH=${PWD}
#SHELL_PATH=${}/`dirname $0`
GIT_ROOT="$(git rev-parse --show-toplevel)"
CODE_STYLE_DIR=$GIT_ROOT/tools/android_code_style
cp $CODE_STYLE_DIR/code-style-checker.py $GIT_ROOT/.git/hooks/pre-commit
chmod u+x,g+x,o+x $GIT_ROOT/.git/hooks/pre-commit
