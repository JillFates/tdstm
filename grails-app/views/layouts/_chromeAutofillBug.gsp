    <%--
        TM-14282 - Fix for Chrome bug with autofill introduced in Chromium version 71/72
        See https://bugs.chromium.org/p/chromium/issues/detail?id=932239
    --%>
    <input type="text" value="chrome-broke-the-web" tabindex="-1"
        style="position: absolute; clip: rect(0, 0, 0, 0); overflow: hidden; width: 1px !important; height: 1px !important; margin: 0 -1px -1px 0 !important; white-space: nowrap;"
    />
