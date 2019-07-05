# Maven 打包自定义 Property
## 背景
继上次的需求，`JWT signingKey` 需要每次动态生成指定长度的随机数，使用了` Spring Boot` 自带的 `${random.int}`，发现不能满足要求，即便在 `Spring Config Server` 统一配置 `${random.int}` 但是其他模块也只是对 `${random.int}` 的引用，然后在各自模块调用各自的 `${random.int}` 表达式生成随机数。
这样各个模块生成不一致的随机数，不满足业务需求。想了想退而求其次，我在 `maven` 打包的时候生成指定长度的随机字符串，应该能满足我的要求。

## 调查编码
Google 了半天，的确有相关的实现方案：第一种方案是是自己自定义一个 `Maven` 插件，在运行插件的目标阶段插入一个 Property 供其他地方使用，相关[参考资料](https://stackoverflow.com/questions/3984794/generating-uuid-through-maven);第二种方案，直接找一个现成的插件，[参考资料](https://stackoverflow.com/questions/3984794/generating-uuid-through-maven)
这里就就直接采用第二种方案，来的比较快，主要配置：

```xml
<plugins>
    <plugin>
        <artifactId>maven-resources-plugin</artifactId>
        <executions>
            <execution> <!-- 复制配置文件 -->
                <id>copy-resources</id>
                <phase>package</phase>
                <goals>
                    <goal>copy-resources</goal>
                </goals>
                <configuration>
                    <outputDirectory>target/classes</outputDirectory>
                    <useDefaultDelimiters>false</useDefaultDelimiters>
                    <!--设置分隔符-->
                    <delimiters>
                        <delimiter>@</delimiter>
                    </delimiters>
                    <resources>
                        <resource>
                            <directory>src/main/resources/</directory>
                            <filtering>true</filtering>
                            <!--指定扫描替换哪些文件，当然也可以用exclude来排除文件-->
                            <includes>
                                <include>**/*.yml</include>
                                <include>**/*.properties</include>
                            </includes>
                        </resource>
                    </resources>
                    <outputDirectory>${project.build.directory}</outputDirectory>
                </configuration>
            </execution>
        </executions>
    </plugin>
<plugin>
    <groupId>org.codehaus.gmaven</groupId>
    <artifactId>gmaven-plugin</artifactId>
    <version>1.3</version>
    <executions>
        <execution>
            <id>set-signKey</id>
            <phase>initialize</phase>
            <goals>
                <goal>execute</goal>
            </goals>
            <configuration>
                <classpath>
                <!--引用依赖-->
                    <element>
                        <groupId>commons-lang</groupId>
                        <artifactId>commons-lang</artifactId>
                        <version>2.6</version>
                    </element>
                </classpath>
                <source>
                <!--重点代码，生成名叫signingKey的property，value为128位的随机字符串-->
                    import org.apache.commons.lang.RandomStringUtils
                    project.properties.setProperty('signingKey', RandomStringUtils.random(128,true,true))
                </source>
            </configuration>
        </execution>
    </executions>
</plugin>
```

在 `application.yml` 中引用

```yml
# *.yml配置文件这里要加上双引号或单引号否则 Spring Boot 解析 yml 时会报错
# *.properties 可不加双引号或单引号
signingKey: "@signingKey@"
```

## 运行效果

```json
{
    "signingKey": "gvx8NET6pLauCLglerpLexLKtHc8HzNisLAe8g9nZCsoNuOlIpkkAKBIsKK62s1nLg18kPm61msJ8QihENvwE3NWCV3xGQamdUJNVX7RVMkPhJEPwFzEgIC2Z0qN56YH"
}
```

查看编译打包后的配置文件 `@signingKey@` 已经被替换成了 `128` 位的随机字符串

[Github 源码](https://github.com/zealzhangz/custom-maven-proverty)