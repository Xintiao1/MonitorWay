#!/bin/sh

./releaseDB.sh $1
mv syncDB.sql $1/initScript

./releaseTable.sh
mv syncTable.sql $1/initScript

cd $1
tar -czf initScript.tar.gz initScript

#VERSION=`cat $1/1.VersionManage/ReleaseVersion.txt`
#VERSION="release-"$VERSION

#git branch -D $VERSION
#git checkout -b $VERSION
#git push origin $VERSION