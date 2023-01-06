@echo off
echo 注意事项：项目依赖于Hadoop环境，以及JDK1.8，如果计算机存在依赖项，那么将不用过于担心兼容性问题。
echo ">>> Spark--Master <<<"
set /p Master=
echo ">>> Spark-AppName <<<"
set /p AppName=
echo "输入测试结果展示"

echo %Master%

echo %AppName%

pause
exit