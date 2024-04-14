# Gitlet Design Document

**Name**: Baoheng Zhu

## Classes and Data Structures



### Blob
在.gitlet/blobs下存储blob对象

#### Features：

1. content -- 序列化的文件内容
2. hash -- 这个blob所对应的hash，只应该计算一次

#### Functions

1. calcHash -- 计算这个blob的hash
2. save -- 将这个blob作为文件永久保存
3. getHash -- 返回这个blob的hash

### Branch
在.gitlet/branches下存储branch对象

本质是一个hashmap,需要的时候作为hashmap提取出来使用

#### Functions

1. setBranch -- 用branch名字命名，branch指针指向的内容作为commit
2. getBranch -- 通过branch的名字，获得该branch最新的commit

### Commit
在.gitlet/commits下存储commit对象

#### Features
1. parent -- 没有合并时指向的parent提交
2. mergeParent -- 有合并时，合并过来的parent提交
3. message -- 创建commit时提交的信息
4. timestamp -- 时间戳
5. blobMap -- 文件名与其对应的hashmap
6. hash -- 这个commit的hash

#### Functions

1. calcHash -- 计算这个commit的hash
2. save -- 将这个commit作为文件永久保存
3. getHash -- 返回这个commit的hash
4. print -- 返回使用log方法时打印的内容
5. getBlobMap -- 返回这个commit中的blobMap
6. load -- 输入Uid， 返回commit对象


### HEAD
在.gitlet/HEAD下存储当前的head指针

1. setHead -- 将head指针存入HEAD文件中
2. getHead -- 从HEAD文件中读取head指针

### StagingArea
在.gitlet/stagingArea下存储暂存区的哈希表文件

#### Features
维护两张表
addition -- Hashmap 要添加的东西以文件名-hash的映射形式存储
removal -- Hashset 存储要删除的文件名

1. load
2. save
3. clear
4. getAddition
5. getRemoval

## Algorithms

## Persistence

merge方法： 

找分支点（split point）
- 如果分支点与given分支指向的commit相同，则打印信息，退出
- 如果分支点和当前branch分支指向的commit相同，则checkout到given的分支，打印信息，退出

在split point后，
1. 如果一个文件在given被modified，但是current没有，这些都应该被更新到given的版本（checkout）
2. 与2相反，如果一个文件在current被modified,但是given没有，那就维持在current的版本
3. 如果一个文件在两个分支中都被modified，并且更改后的内容也完全相同，同样维持不变。如果在两个分支中都删除了这个文件，但是在cwd中依然
存在，就让他存在，保持untracked的状态
4. 如果split point中没有的文件，其中current分支独有，那就继续保留它
5. 如果split point中没有的文件，其中given分支独有，那就checkout出来
6. 如果split point中的文件，在current分支没动，但是在given分支中不见了
，那就给它删除
7. 如果split point中的文件，在given分支没动，但是在current分支中不见了
，那就保持丢失的状态不动
8. 冲突：
- 在given分支和current分支都更改且更改方式不同
- 在其中一个分支被更改而另一个分支中被删除
- 在split point中不存在的文件，在given分支和current分支中有不同的内容

错误情况：
- staging area不为空
- given branch不存在
- given branch与current branch相同
- 有untracked file
