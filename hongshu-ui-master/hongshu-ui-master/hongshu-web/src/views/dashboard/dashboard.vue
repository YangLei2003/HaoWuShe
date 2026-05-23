<template>
  <!--  模板部分,用于写Html结构-->
  <div class="feeds-page">
    <div class="channel-container">
      <div class="scroll-container channel-scroll-container">
        <div class="content-container">
<!--          如果categoryClass==0 则选择类名channel active  否则选择channel
              当点击这个div的时候,执行getNoteList方法
-->
          <div :class="categoryClass == '0' ? 'channel active' : 'channel'" @click="getNoteList">推荐</div>
<!--          第一个固定是推荐,后面的分类通过遍历categoryList列表来显示-->
<!--          点击分类按钮对应的div会执行getNoteListByCategory方法,并且根据传入的ID进行业务逻辑-->
          <div
              :class="categoryClass == item.id ? 'channel active' : 'channel'"
              v-for="item in categoryList"
              :key="item.id"
              @click="getNoteListByCategory(item.id)"
          >
            {{ item.title }}
          </div>
        </div>
      </div>
    </div>
    <div class="loading-container"></div>
    <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
      <div class="feeds-loading-top animate__animated animate__zoomIn animate__delay-0.5s" v-show="topLoading">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
      <Waterfall

          :list="noteList"
          :width="options.width"
          :gutter="options.gutter"
          :hasAroundGutter="options.hasAroundGutter"
          :animation-effect="options.animationEffect"
          :animation-duration="options.animationDuration"
          :animation-delay="options.animationDelay"
          :breakpoints="options.breakpoints"
          style="min-width: 740px"
      >
<!--        Waterfall组件特有的,内部多个template标签,-->
<!--        第一个template,由waterfall定义的插槽模板-->
        <template #item="{ item }">
          <div style="display:none">{{ console.log(item) }}</div>
          <el-skeleton style="width: 240px" :loading="!item.isLoading" animated>
<!--            骨架屏,用于数据还未加载出来时的灰色画面渲染-->
            <!-- 最后执行handleLoad(item)方法,内部就是将load设为true,这样瀑布组件就可以显示加载完成后的图片了-->
<!--            @Load是Vue的事件监听,专用于监听图片加载完成的事件-->
            <template #template>
              <el-image
                  :src="item.noteCover"
                  :style="{
                  width: '240px',
                  maxHeight: '300px',
                  height: item.noteCoverHeight + 'px',
                  borderRadius: '8px',
                }"
                  @load="handleLoad(item)"
              ></el-image>
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 100%"/>
                <div style="display: flex; align-items: center; margin-top: 2px; height: 16px">
                  <el-skeleton style="--el-skeleton-circle-size: 20px">
<!--                    el-skeleton-item : 表示骨架屏的单个占位元素-->
                    <template #template>
                      <el-skeleton-item variant="circle"/>
                    </template>
                  </el-skeleton>
                  <el-skeleton-item variant="text" style="margin-left: 10px"/>
                </div>
              </div>
            </template>
<!--数据加载完成后显示-->
            <template #default>
              <div class="card" style="max-width: 240px">
<!--                fit="cover"  -> 确保图片填充整个容器,不留白-->
                <el-image
                    :src="item.noteCover"
                    :style="{
                    width: '240px',
                    maxHeight: '300px',
                    height: item.noteCoverHeight + 'px',
                    borderRadius: '8px',
                  }"
                    fit="cover"
                    @click="toMain(item.id)"
                ></el-image>
<!--                点击图片调用toMain方法,使组件Main根据noteId显示-->
                <div class="footer">
                  <a class="title">
                    <span>{{ item.title }}</span>
                  </a>
                  <div class="author-wrapper">
                    <a class="author">
                      <img class="author-avatar" :src="item.avatar"/>
                      <span class="name">{{ item.username }}</span>
                    </a>
                    <span class="like-wrapper like-active">
                      <i
                          class="iconfont icon-follow-fill"
                          style=" width: 1em; height: 1em; color:red"
                          v-if="item.isLike"
                      >
                      </i>
                      <i class="iconfont icon-follow" style="width: 1em; height: 1em" v-else></i>

                      <span class="count">{{ item.likeCount }}</span>
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-skeleton>
        </template>
      </Waterfall>

      <div class="feeds-loading" v-show="!isEnd">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
      <div class="feeds-end" v-show="isEnd">······ 已经到底了 ·····</div>
    </div>
    <FloatingBtn @click-refresh="refresh"></FloatingBtn>
    <!--动态绑定属性    :属性名="变量名"   如"nid="nid""-->
    <!--  绑定事件     @事件名="方法名"   如@click-main="close"-->
<!--    @click-main="close"监听子组件出发的自定义事件@Click-main-->

<!--    将这个组件是否显示的show绑定到mainShow的值上,然后将noteId和time传给main组件-->
    <Main
        v-show="mainShow"
        :nid="nid"
        :nowTime="new Date()"
        class="animate__animated animate__zoomIn animate__delay-0.5s"
        @click-main="close"
    ></Main>

  </div>
</template>

<script lang="ts" setup>//用于写JavaScript逻辑的地方
import {Waterfall} from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import {ref, watch} from "vue";
import {getRecommendNote, getNoteByDTO, addRecord} from "@/api/search";
import {getCategoryTreeData} from "@/api/category";
import type {NoteDTO, NoteSearch} from "@/type/note";
import type {Category} from "@/type/category";
import Main from "@/views/main/main.vue";
import FloatingBtn from "@/components/FloatingBtn.vue";
import {options} from "@/constant/constant";
import {useSearchStore} from "@/store/searchStore";
import Loading from "@/components/Loading.vue";
import {refreshTab} from "@/utils/util";
//定义响应式变量(ref)
const searchStore = useSearchStore();//一个全局搜索状态,是现成的.管理搜索关键词,搜索种子(出发搜索的信号),搜索历史等
const topLoading = ref(false);
const noteList = ref<Array<NoteSearch>>([]);//定义笔记列表,初始为空列表
const categoryList = ref<Array<Category>>([]);
const currentPage = ref(1);
const pageSize = 100;
const noteTotal = ref(0);
const categoryClass = ref("0");
const mainShow = ref(false);
const nid = ref("");
const isEnd = ref(false);
const noteDTO = ref<NoteDTO>({
  keyword: "",
  type: 0,
  cid: "",
  cpid: "",
});

watch(
    () => [searchStore.seed],//监听searchStore.seed变量,就是监听用户是否按下快捷键
    () => {
      noteDTO.value.keyword = searchStore.keyWord;//监听到seed变化,则将searchStore.keyWord赋给noteDTO.value.keyword
      noteDTO.value.cpid = "";//清空分类
      categoryClass.value = "0";//回到默认分类
      getNoteListByKeyword();
      addRecord(searchStore.keyWord);//增加记录
    }
);

//定义方法
const toMain = (noteId: string) => {
  nid.value = noteId;
  mainShow.value = true;
};

const close = () => {
  mainShow.value = false;
};

const handleLoad = (item: any) => {
  item.isLoading = true;
};

const refresh = () => {
  refreshTab(() => {
      currentPage.value = 1;
      noteList.value = [];
      getNoteList();
      topLoading.value = false;
  });
};

const loadMoreData = () => {
  if (noteList.value.length >= noteTotal.value) {
    isEnd.value = true;
    return; // 如果已经加载完所有数据，则不再请求
  }
  currentPage.value += 1;
  if (noteDTO.value.cpid === "" && noteDTO.value.keyword == "") {
    getRecommendNote(currentPage.value, pageSize).then((res: any) => {//res就是后端传给前端的JSON数据,包含code msg data 等
      setData(res);
    });
  } else {
    getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
      setData(res);
    });
  }
};

const setData = (res: any) => {//相当于void setData(Object res),用于接收参数
  const {records, total} = res.data;//接收res,从中提取data字段,并且赋值给records和total,参数名必须要提前约定好
  noteTotal.value = total;
  if (records.length === 0) {
    isEnd.value = true;
  } else {
    noteList.value.push(...records);//向noteList末尾中添加数据
  }
};

const getNoteList = async () => {
  categoryClass.value = "0";
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  getRecommendNote(currentPage.value, pageSize).then((res: any) => {
    setData(res);
  });
};

const getNoteListByCategory = (id: string) => {
  categoryClass.value = id;
  noteDTO.value.cpid = id;
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  //调用getNoteByDTO,向后端传入参数,等待后端回复的JSON数据,拿到数据调用setData方法
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getNoteListByKeyword = () => {
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getCategoryData = () => {
  getCategoryTreeData().then((res: any) => {
    categoryList.value = res.data;
  });
};

const initData = () => {
  getCategoryData();
  getNoteList();
};

initData();
</script>

<style lang="less" scoped>
.feeds-page {
  flex: 1;
  padding: 0 24px;
  padding-top: 72px;
  height: 100vh;

  .channel-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    user-select: none;
    -webkit-user-select: none;

    .channel-scroll-container {
      backdrop-filter: blur(20px);
      background-color: transparent;
      width: calc(100vw - 24px);

      position: relative;
      overflow: hidden;
      display: flex;
      user-select: none;
      -webkit-user-select: none;
      align-items: center;
      font-size: 16px;
      color: rgba(51, 51, 51, 0.8);
      height: 40px;
      white-space: nowrap;
      height: 72px;

      .content-container::-webkit-scrollbar {
        display: none;
      }

      .content-container {
        display: flex;
        overflow-x: scroll;
        overflow-y: hidden;
        white-space: nowrap;
        color: rgba(51, 51, 51, 0.8);

        .active {
          font-weight: 600;
          background: rgba(0, 0, 0, 0.03);
          border-radius: 999px;
          color: #333;
        }

        .channel {
          height: 40px;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 0 16px;
          cursor: pointer;
          -webkit-user-select: none;
          user-select: none;
        }

        :hover {
          cursor: pointer; /* 显示小手指针 */
          transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
        }
      }
    }
  }

  .feeds-container {
    position: relative;
    transition: width 0.5s;
    margin: 0 auto;

    .feeds-loading {
      margin: 3vh;
      text-align: center;
    }

    .feeds-loading-top {
      text-align: center;
      line-height: 6vh;
      height: 6vh;
    }

    .noteImg {
      width: 240px;
      max-height: 300px;
      object-fit: cover;
      border-radius: 8px;
    }

    .footer {
      padding: 12px;

      .title {
        margin-bottom: 8px;
        word-break: break-all;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        font-weight: 500;
        font-size: 14px;
        line-height: 140%;
        color: #333;
      }

      .author-wrapper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 20px;
        color: rgba(51, 51, 51, 0.8);
        font-size: 12px;
        transition: color 1s;

        .author {
          display: flex;
          align-items: center;
          color: inherit;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-right: 12px;

          .author-avatar {
            margin-right: 6px;
            width: 20px;
            height: 20px;
            border-radius: 20px;
            border: 1px solid rgba(0, 0, 0, 0.08);
            flex-shrink: 0;
            object-fit: cover;
          }

          .name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .like-wrapper {
          position: relative;
          cursor: pointer;
          display: flex;
          align-items: center;

          .count {
            margin-left: 2px;
          }
        }
      }
    }
  }

  .floating-btn-sets {
    position: fixed;
    display: flex;
    flex-direction: column;
    width: 40px;
    grid-gap: 8px;
    gap: 8px;
    right: 24px;
    bottom: 24px;

    .back-top {
      width: 40px;
      height: 40px;
      background: #fff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-radius: 100px;
      color: rgba(51, 51, 51, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      // transition: background 0.2s;
      cursor: pointer;
    }

    .reload {
      width: 40px;
      height: 40px;
      background: #fff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1),
      0 1px 2px 0 rgba(0, 0, 0, 0.02);
      border-radius: 100px;
      color: rgba(51, 51, 51, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      //transition: background 0.2s;
      cursor: pointer;
    }
  }

  .feeds-end {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50px;
    color: #999;
    font-size: 16px;
    margin-top: 20px;
    border-radius: 10px;
  }
}
</style>
