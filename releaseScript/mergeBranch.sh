#!/bin/sh

workspace=$1
version=$2
default_branch=mwmonitor-1.0

cd $workspace

git checkout $default_branch
git branch --set-upstream-to=origin/$default_branch
git checkout -f
git pull

#判断分支是否存在
branch=`git branch | grep $version`

if [ -n "$branch" ]; then
    git checkout $version
    git branch --set-upstream-to=origin/$version
    git pull
    git merge --no-ff mwmonitor-1.0
    git commit -m "merge to $version"
    git push
else
    git checkout -b $version
    git push origin $version
fi