/* eslint-disable @typescript-eslint/no-explicit-any */
declare module "@mdxeditor/editor" {
  import type { ComponentType } from "react";
  export const MDXEditor: ComponentType<any>;
  export type MDXEditorProps = any;
  export const headingsPlugin: (...args: any[]) => any;
  export const listsPlugin: (...args: any[]) => any;
  export const quotePlugin: (...args: any[]) => any;
  export const linkPlugin: (...args: any[]) => any;
  export const imagePlugin: (...args: any[]) => any;
  export const tablePlugin: (...args: any[]) => any;
  export const codeBlockPlugin: (...args: any[]) => any;
  export const markdownShortcutPlugin: (...args: any[]) => any;
  // 额外导出：工具栏与高级特性
  export const frontmatterPlugin: (...args: any[]) => any;
  export const thematicBreakPlugin: (...args: any[]) => any;
  export const diffSourcePlugin: (...args: any[]) => any;
  export const linkDialogPlugin: (...args: any[]) => any;
  export const directivesPlugin: (...args: any[]) => any;
  export const AdmonitionDirectiveDescriptor: any;
  export const toolbarPlugin: (...args: any[]) => any;
  export const UndoRedo: any;
  export const BoldItalicUnderlineToggles: any;
  export const CodeToggle: any;
  export const ListsToggle: any;
  export const CreateLink: any;
  export const InsertTable: any;
  export const InsertImage: any;
  export const InsertCodeBlock: any;
  export const InsertThematicBreak: any;
  export const BlockTypeSelect: any;
  export const DiffSourceToggleWrapper: any;
  export const Separator: any;
  export const StrikeThroughSupSubToggles: any;
}

declare module "@mdxeditor/editor/style.css" {}