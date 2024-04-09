import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: "zh-CN",
  title: "享存",
  description: "一款可以将分享内容保存为文件的应用",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    logo: { light: "/images/logo.svg", dark: "/images/logo-dark.svg" },
    nav: [
      { text: "主页", link: "/" },
      { text: "使用帮助", link: "/help" },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/MeanZhang/Shave" },
    ],
    footer: {
      copyright: "Copyright © 2022-2024 Mean",
    },
    search: {
      provider: "local",
    },
  },
});
