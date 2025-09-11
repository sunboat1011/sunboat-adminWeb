#!/bin/bash

# 检查参数是否提供完整
if [ $# -ne 3 ]; then
    echo "使用方法: $0 <目标文件夹路径> <要替换的字符串> <替换后的字符串>"
    echo "示例: $0 ./myproject adminWeb template"
    exit 1
fi

TARGET_DIR="$1"
OLD_STRING="$2"
NEW_STRING="$3"

# 检查目标文件夹是否存在
if [ ! -d "$TARGET_DIR" ]; then
    echo "错误: 文件夹 '$TARGET_DIR' 不存在"
    exit 1
fi

echo "开始处理文件夹: $TARGET_DIR"
echo "将替换所有 '$OLD_STRING' 为 '$NEW_STRING'"

# 第一步：替换所有文件内容中的指定字符串
echo "正在替换文件内容..."
find "$TARGET_DIR" -type f -exec sed -i '' "s/$OLD_STRING/$NEW_STRING/g" {} +

# 第二步：替换所有文件名称中的指定字符串
echo "正在重命名文件..."
find "$TARGET_DIR" -depth -type f -name "*$OLD_STRING*" | while read -r file; do
    dir=$(dirname "$file")
    base=$(basename "$file")
    new_base="${base//$OLD_STRING/$NEW_STRING}"
    if [ "$base" != "$new_base" ]; then
        mv -v "$file" "$dir/$new_base"
    fi
done

# 第三步：替换所有文件夹名称中的指定字符串
echo "正在重命名文件夹..."
find "$TARGET_DIR" -depth -type d -name "*$OLD_STRING*" | while read -r dir; do
    parent=$(dirname "$dir")
    base=$(basename "$dir")
    new_base="${base//$OLD_STRING/$NEW_STRING}"
    if [ "$base" != "$new_base" ]; then
        mv -v "$dir" "$parent/$new_base"
    fi
done

echo "所有替换操作已完成"
