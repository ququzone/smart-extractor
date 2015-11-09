<!DOCTYPE html>
<html lang="en">
<head>
    <title>Smart Extractor</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/fetch/0.10.1/fetch.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/0.14.2/react.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/0.14.2/react-dom.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-core/5.8.23/browser.min.js"></script>
</head>
<body>
<div>
    <h2>Extract main content form web</h2>
    <div id="extract">
    </div>
</div>
<script type="text/babel">
    var ExtractForm = React.createClass({
        handleSubmit: function(e) {
            e.preventDefault();
            var url = this.refs.url.value.trim();
            if (!url) {
                return;
            }
            var that = this;
            fetch('/extract?url=' + encodeURIComponent(url))
                .then(function(res) {
                    return res.json();
                }).then(function(json) {
                    that.refs.url.value = '';
                    if (json.success) {
                        that.props.onExtracted(json.title, json.html);
                    } else {
                        that.props.onExtracted('error', json.msg);
                    }
                });
        },

        render: function() {
            return (
                <div>
                    <form onSubmit={this.handleSubmit}>
                        <div>
                            <label>URL: </label>
                            <input name="url" type="text" placeholder="url" ref="url" />
                        </div>
                        <div>
                            <button id="extract-btn">extract</button>
                        </div>
                    </form>
                </div>
            );
        }
    });
    var ExtractContent = React.createClass({
        render: function() {
            return (
                <div>
                    <hr />
                    <div>
                        <h2>{this.props.title}</h2>
                    </div>
                    <div dangerouslySetInnerHTML={{__html: this.props.content}} />
                </div>
            );
        }
    });
    var ExtractApp = React.createClass({
        getInitialState: function() {
            return {title: 'NONE', content: ''};
        },

        onExtracted: function(title, content) {
            this.setState({title: title || "", content: content || ""});
        },

        render: function() {
            return (
                <div>
                    <ExtractForm onExtracted={this.onExtracted} />
                    <ExtractContent title={this.state.title} content={this.state.content} />
                </div>
            );
        }
    });
    ReactDOM.render(
        <ExtractApp />,
        document.getElementById('extract')
    );
</script>
</body>
</html>