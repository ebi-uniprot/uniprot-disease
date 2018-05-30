# Multi-build Dockerfile
# 1- complie source code and create target jar
# 2- copy jar file, get datafile -> import into database and create entry point

FROM maven as disease_compile
WORKDIR /usr/src/disease
COPY ./pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM java:8-jre-alpine AS disease_api
LABEL maintainer <rizwan.ishtiaq@ebi.ac.uk>
WORKDIR /app
COPY --from=disease_compile /usr/src/disease/target/uniprot-disease-0.0.1-SNAPSHOT.jar .

#Below required to run embaded mangodb into container
ENV LANG=C.UTF-8
ENV LC_ALL=C

# Here we install GNU libc (aka glibc), libstdc++ and set C.UTF-8 locale as default. 
RUN ALPINE_GLIBC_BASE_URL="https://github.com/sgerrand/alpine-pkg-glibc/releases/download" && \
 ALPINE_GLIBC_PACKAGE_VERSION="2.27-r0" && \
 ALPINE_GLIBC_BASE_PACKAGE_FILENAME="glibc-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
 ALPINE_GLIBC_BIN_PACKAGE_FILENAME="glibc-bin-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
 ALPINE_GLIBC_I18N_PACKAGE_FILENAME="glibc-i18n-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
 apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
 wget \
 "https://raw.githubusercontent.com/sgerrand/alpine-pkg-glibc/master/sgerrand.rsa.pub" \
 -O "/etc/apk/keys/sgerrand.rsa.pub" && \
 wget \
 "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_I18N_PACKAGE_FILENAME" && \
 apk add --no-cache \
 "$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_I18N_PACKAGE_FILENAME" && \
 \
 rm "/etc/apk/keys/sgerrand.rsa.pub" && \
 /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 "$LANG" || true && \
 echo "export LANG=$LANG" > /etc/profile.d/locale.sh && \
 \
 apk del glibc-i18n && \
 \
 rm "/root/.wget-hsts" && \
 apk del .build-dependencies && \
 rm \
 "$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
 "$ALPINE_GLIBC_I18N_PACKAGE_FILENAME" && \
 apk apk update && apk add libstdc++ && \
 wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/humdisease.txt && \
 java -jar uniprot-disease-0.0.1-SNAPSHOT.jar humdisease.txt --stopserver && \
 rm humdisease.txt

EXPOSE 8080
ENTRYPOINT ["java","-jar","uniprot-disease-0.0.1-SNAPSHOT.jar"]