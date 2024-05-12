#!/bin/sh

workspace=$1
version=$2
syncDBfile=$3
syncTablefile=$4

cd $workspace

if [ ! -d "monitordb" ]; then
    git clone git@sede0899.monitorway.net:dev/monitordb.git
    cd monitordb
    git branch --set-upstream-to=origin/master
else
    cd monitordb
fi

git checkout -f

#判断分支是否存在
branch=`git branch | grep $version`
hasBranch=false

if [ -n "$branch" ]; then
    hasBranch=true
    git checkout $version
    git branch --set-upstream-to=origin/$version
    git pull
else
    git checkout -b $version
fi

echo "$branch:$hasBranch"

mv $syncDBfile $workspace/monitordb
mv $syncTablefile $workspace/monitordb

git add --all
git commit -m "$version"
git push origin $version

