if [ -z "$1" ]; then
    echo "Usage: cl-server <locator host>"
    exit 1
fi

loc=$1[10334]


hn=`hostname`
target=/Users/zhansen/tmp/client-group/corelogic/demos/data-microservice-demo/demographic-gemfire-client/target

for i in `ls $target/dependency/*.jar`; do 
    cp=$cp:$i
done
echo $cp

jvmargs="--J=-XX:+UseParNewGC --J=-XX:+UseConcMarkSweepGC --J=-XX:CMSInitiatingOccupancyFraction=60"
gfargs="--J=-Dgemfire.statistic-archive-file=$hn.gfs --J=-Dgemfire.archive-file-size-limit=100 --J=-Dgemfire.archive-disk-space-limit=1000  --J=-Dgemfire.start-dev-rest-api=true --J=-Dgemfire.http-service-port=8888"

gfsh -e "connect" -e "configure pdx --read-serialized=true"
gfsh start server --name=server3-$hn  --server-port=0 --locators=$loc --initial-heap=1g --max-heap=1g $jvmargs $gfarg --classpath=$cp 