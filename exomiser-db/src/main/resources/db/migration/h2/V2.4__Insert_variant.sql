INSERT INTO variant SELECT * FROM CSVREAD('${import.path}/variant.pg', 'chromosome|position|ref|alt|aaref|aaalt|aapos|sift|polyphen|mut_taster|phylop|cadd|cadd_raw','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
