#!/bin/bash
for f in *.xsd
do
xsltproc --stringparam title "$f" \
 --stringparam searchIncludedSchemas true \
 --stringparam searchImportedSchemas true \
 --stringparam linksFile links.xml \
 xs3p.xsl $f > $f.html
done

