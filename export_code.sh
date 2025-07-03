#!/bin/bash

# Tên của file output
OUTPUT_FILE="full_project_code.txt"

# Xóa file output cũ nếu tồn tại để bắt đầu một file mới
> "$OUTPUT_FILE"

echo "Bắt đầu tổng hợp code vào file: $OUTPUT_FILE"

# Sử dụng `find` để tìm tất cả các file .java và .yml
# -prune dùng để loại bỏ các thư mục không cần thiết như target, .git
find . -type d \( -name "target" -o -name ".git" -o -name ".idea" -o -name ".mvn" \) -prune -o -type f \( -name "*.java" -o -name "*.yml" -o -name "*.properties" -o -name "pom.xml" \) -print | while IFS= read -r file; do
  
  # In tên file vào file output để làm dấu phân cách
  echo "======================================================================" >> "$OUTPUT_FILE"
  echo "FILE: $file" >> "$OUTPUT_FILE"
  echo "======================================================================" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
  
  # Nối nội dung của file tìm được vào file output
  cat "$file" >> "$OUTPUT_FILE"
  
  # Thêm hai dòng trống để ngăn cách các file cho dễ đọc
  echo "" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"

done

echo "Hoàn thành! Toàn bộ code đã được ghi vào file: $OUTPUT_FILE"