FROM python:3.7

LABEL maintainer="jackleeforce@gmail.com"

# ENV DEBIAN_FRONTEND=noninteractive \
#     TZ=Asia/Shanghai

WORKDIR /mobile_capture_server

RUN pip install pipenv -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
    # && python3 -m pip install gunicorn -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
    # && python3 -m pip install gevent -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
    && pipenv sync 

# RUN apt-get update -qq \
#     && apt-get install -y tzdata locales python3-pip python3-dev \
#     && ln -s /usr/bin/python3 /usr/local/bin/python \
#     && pip3 install --no-cache-dir --upgrade pip \
#     && python3 -m pip install pipenv -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
#     && python3 -m pip install gunicorn -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
#     && python3 -m pip install gevent -i https://pypi.douban.com/simple/ --trusted-host pypi.douban.com \
#     && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
#     && echo $TZ > /etc/timezone \
#     && locale-gen zh_CN.UTF-8 \
#     && dpkg-reconfigure --frontend noninteractive tzdata locales \
#     && apt-get clean

# ENV LANG=zh_CN.UTF-8 \
#     LANGUAGE=zh_CN:zh \
#     LC_ALL=zh_CN.UTF-8

CMD ["pipenv", "run", "gunicorn", "-c", "gunicorn_conf.py", "app:app"]


