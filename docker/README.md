### Build local 
docker build -f docker/Dockerfile -t disease_api https://github.com/ebi-uniprot/uniprot-disease.git

### Multi-build Dockerfile
1. complie source code and create target jar
1. copy jar file, install glibc and libstdc++, get datafile -> import into database and create entry point

### Actions
1. Above command let latest code from repo
1. Complie it
1. Emdaded mongodb required GNU libc (aka glibc) and libstdc++ also setting C.UTF-8 locale as default
1. Get current lastest datafile from EBI file server for diseases
1. create database and import data in to database from file
1. Removing disease data file
1. expose port 8080 inside container to listen and entertain user request

### Clean up
> If you are uing above command to build image locally, because of multi build it will create and extra image of size aprox 1 GB with repository none. You can optionally delete that image. One good way to check your images before and after run of above command

Need any help regarding git commands see [git](https://github.com/rizwan-ishtiaq/wiki/blob/master/commands/docker.txt) for quick reference.