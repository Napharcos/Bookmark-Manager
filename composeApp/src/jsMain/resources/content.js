function gatherPageInfo() {
    const favs = [...document.querySelectorAll("link[rel*='icon']")].map(l => l.href);
    const metaImages = [
        document.querySelector("meta[property='og:image']")?.content,
        document.querySelector("meta[name='twitter:image']")?.content
    ].filter(Boolean);
    const pageImages = [...document.querySelectorAll("img")].map(i => i.src);

    const data = {
        fullUrl: window.location.href,
        baseDomain: window.location.hostname.split(".").slice(-2).join("."),
        title: document.title,
        favicons: favs,
        metaImages,
        pageImages
    };

    chrome.runtime.sendMessage({ type: "PAGE_INFO", payload: data });
}

gatherPageInfo();