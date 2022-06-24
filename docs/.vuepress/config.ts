import { defaultTheme, defineUserConfig } from "vuepress";

export default defineUserConfig({
  lang: "zh-CN",
  title: "享存",
  description: "享存帮助文档",
  head: [["link", { rel: "icon", href: "/images/logo.svg" }]],

  theme: defaultTheme({
    logo: "/images/logo.svg",
    repo: "MeanZhang/Shave",
    docsBranch: "main",
    docsDir: "docs",
    editLinkText: "编辑此页面",
    lastUpdatedText: "上次更新于",
    contributorsText: "贡献者",
    navbar: [
      {
        text: "服务协议",
        link: "/agreement",
      },
      {
        text: "隐私政策",
        link: "/privacy",
      },
    ],
  }),
});
