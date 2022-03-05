---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: frontpage
title: Tripoli
description: Web presence for the development of Tripoli software.
---
<div class="navbar">
  <div class="navbar-inner">
      <ul class="nav">
          <li><a href="https://github.com/CIRDLES/Tripoli">github</a></li>
          <li><a href="https://github.com/CIRDLES/Tripoli/discussions">discussions</a></li>
          <li><a href="https://twitter.com/Tripoli_ET">@Tripoli_ET</a></li>
      </ul>
  </div>
</div>

#### Welcome to the Tripoli software development home page.

We will be posting updates here2:
<ul>
  {% for post in site.posts %}
    <li>
      <a href="{{ site.JB.BASE_PATH }}{{ post.url }}">{{ post.title }}</a>
    </li>
  {% endfor %}
</ul>
