#### gcov 运行结果比较

docker 上安装的是 gcc 6，我的windows上安装的是 gcc 7，看起来这两个版本生成的 gcov
的运行结果是不一样的：

##### docker
```gcov
        1:   87:                                        (void)fputs( "* EOF in table *\n",
        -:   88:                                                     stdout
```

##### windows
```gcov
        1:   87:					(void)fputs( "* EOF in table *\n",
        1:   88:						     stdout
```

##### 总结
这里的差异并不影响运行结果，可以忽略不计
