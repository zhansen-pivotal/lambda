if [ -z "$1" ]; then
    echo "Usage: cl-locator <locator host>"
    exit 1
fi

loc=$1[10334]


hn=`hostname`


gfargs="--J=-Dgemfire.statistic-archive-file=$hn.gfs --J=-Dgemfire.archive-file-size-limit=100 --J=-Dgemfire.archive-disk-space-limit=1000  --J=-Dgemfire.http-service-port=7575"

gfsh start locator --name=loc-$hn --locators=$loc --initial-heap=1g --max-heap=1g $jvmargs $gfargs