declare module "fast-diff" {
  const Diff: {
    (a: string, b: string): Array<[number, string]>;
    DELETE: number;
    INSERT: number;
    EQUAL: number;
  };
  export default Diff;
}