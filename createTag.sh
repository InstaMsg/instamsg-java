VERSION=`grep INSTAMSG_VERSION common/instamsg/driver/InstaMsg.java | cut -d\  -f 6 | cut -d\" -f 2`
git tag ${VERSION}
git push --tags
