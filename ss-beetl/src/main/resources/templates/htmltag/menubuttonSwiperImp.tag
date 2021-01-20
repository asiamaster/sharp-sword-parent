<link rel="stylesheet"  href="${contextPath}/webjars/swiper/4.5.0/dist/css/swiper.min.css">
<style>
    #.${_divId} {
        position: relative;
    }
    #${_containerId!"swiperContainer"} {
         height: 42px;
     }

    @media only screen and (min-width: 1360px) {
    #${_containerId!"swiperContainer"} {
        width: 780px;
    }
    }
    @media only screen and (min-width: 1440px) {
    #${_containerId!"swiperContainer"} {
        width: 860px;
    }
    }
    @media only screen and (min-width: 1900px) {
    #${_containerId!"swiperContainer"} {
        width: 1360px;
    }
    }
    @media only screen and (min-width: 2500px) {
    #${_containerId!"swiperContainer"} {
        width: 1560px;
    }
    }
    .swiper-slide {
        text-align: center;
        /* Center slide text vertically */
        display: -webkit-box;
        display: -ms-flexbox;
        display: -webkit-flex;
        display: flex;
        -webkit-box-pack: center;
        -ms-flex-pack: center;
        -webkit-justify-content: center;
        justify-content: center;
        -webkit-box-align: center;
        -ms-flex-align: center;
        -webkit-align-items: center;
        align-items: center;
    }
    .swiper-button-next,
    .swiper-button-prev {
        width: 16px;
        height: 26px;
        background-size: unset;
        top: 76%;
        outline: none;
    }
    .swiper-button-prev {
        left: 10px;
    }
    .swiper-button-next {
        right: 10px;
    }
    .swiper-button-next.swiper-button-disabled,
    .swiper-button-prev.swiper-button-disabled {
        opacity: 0;
    }

</style>

<script src="${contextPath}/webjars/swiper/4.5.0/dist/js/swiper.min.js"></script>
<script type="text/javascript">
    $(function () {
        var ${_containerId!"swiperContainer"} = new Swiper('#${_containerId!"swiperContainer"}', {
            slidesPerView : 3,
            spaceBetween: 0,
            breakpoints: {
                //当屏幕宽度小于等于1366
                1366: {
                    slidesPerView: ${_slidesPerView!5},
                    slidesPerGroup : ${_slidesPerGroup!1},
                },
                1440: {
                    slidesPerView: ${_slidesPerView!6},
                    slidesPerGroup : ${_slidesPerGroup!1},
                },
                1920: {
                    slidesPerView: ${_slidesPerView!8},
                    slidesPerGroup : ${_slidesPerGroup!1},
                },
                2560: {
                    slidesPerView: ${_slidesPerView!10},
                    slidesPerGroup : ${_slidesPerGroup!1},
                }
            },
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            }/*,
                on: {
                    resize: function(){
                        this.params.width = window.innerWidth - 500;
                        this.update();
                    },
                }*/
        });
    });

</script>