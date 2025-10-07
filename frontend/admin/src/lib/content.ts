export function computePreviewText(content: string): string {
  return content && content.trim().length > 0 ? content : "暂无内容";
}