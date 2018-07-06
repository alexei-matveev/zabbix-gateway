# Zabbix Gateway

Translates  HTTP   POSTs  into  "ZBXD\1"  TCP   Zabbix  Sender/Trapper
Protocoll.

## Usage

One way to start the gateway:

    lein run -H $proxy

Prepare and run JAR for deployment:

    lein uberjar
    java -jar ...

To post the data in Bash:

    url=http://localhost:15001/trap
    txt='[{"host":"h","key":"k","value":"v"}]'
    curl -XPOST -H "Content-Type: application/json" $url -d $txt

In PowerShell:

    $url = "http://localhost:15001/trap"
    $txt = '[{"host":"h","key":"k","value":"v"}]'
    Invoke-WebRequest -Uri $url -Method POST -ContentType "application/json" -Body $txt

The field  Content of  the response object  contains the  typical JSON
Info String from Zabbix.

## License

Copyright © 2018 Alexei Matveev <alexei.matveev@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
