#! /bash/bin

if [[ -z $1 ]]; then
  echo "Argument error"
  exit -1
fi

mkdir tempDir

echo "$@" > tempDir/output.txt
