echo $(git --no-pager blame --line-porcelain $1 |
    awk '/author Not Committed Yet/{if (a && a !~ /author Not Committed Yet/) print a} {a=$0}' |
    awk '{print $3}')
